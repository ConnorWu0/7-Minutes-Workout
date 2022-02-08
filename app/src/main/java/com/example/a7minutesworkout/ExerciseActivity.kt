package com.example.a7minutesworkout

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.a7minutesworkout.databinding.ActivityExerciseBinding
import com.example.a7minutesworkout.databinding.ActivityMainBinding
import com.example.a7minutesworkout.databinding.DialogCustomBackConfirmationBinding
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ExerciseActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private var binding: ActivityExerciseBinding? = null

    private var restTimer: CountDownTimer? = null
    private var restProgress = 0

    private var restTimerDuration: Long = 1

    private var exerciseTimer: CountDownTimer? = null
    private var exerciseProgress = 0

    private var exerciseTimerDuration: Long = 1

    private var exerciseList : ArrayList<ExerciseModel>? = null
    private var currentExercisePosition = -1

    private var textToSpeech: TextToSpeech? = null
    private var player: MediaPlayer? = null

    private var exerciseAdapter: ExerciseStatusAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExerciseBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setSupportActionBar(binding?.toolbarExercise)

        if (supportActionBar != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = "7 Minutes Workout"
        }

        exerciseList = Constants.defaultExerciseList()

        textToSpeech = TextToSpeech(this,this)

        binding?.toolbarExercise?.setNavigationOnClickListener {
            customDialogForBackButton()
        }
        setupRestView()
        setupExerciseStatusRecyclerView()
    }

    override fun onBackPressed() {
        customDialogForBackButton()
    }

    private fun customDialogForBackButton(){
        val customDialog = Dialog(this)
        val dialogBinding = DialogCustomBackConfirmationBinding.inflate(layoutInflater)
        customDialog.setContentView(dialogBinding.root)
        customDialog.setCanceledOnTouchOutside(false)
        dialogBinding.buttonYes.setOnClickListener {
            this@ExerciseActivity.finish()
            customDialog.dismiss()
        }
        dialogBinding.buttonNo.setOnClickListener {
            customDialog.dismiss()
        }

        customDialog.show()
    }

    private fun setupExerciseStatusRecyclerView(){
        binding?.recyclerViewExerciseStatus?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        exerciseAdapter = ExerciseStatusAdapter(exerciseList!!)
        binding?.recyclerViewExerciseStatus?.adapter = exerciseAdapter
    }

    private fun setupRestView(){

        try {
            val soundURI = Uri.parse(
                "android.resource://com.example.a7minutesworkout/"+R.raw.press_start)
            player = MediaPlayer.create(applicationContext, soundURI)
            player?.isLooping = false
            player?.start()
        }catch (e : Exception){
            e.printStackTrace()
        }

        binding?.frameLayoutRestView?.visibility = View.VISIBLE
        binding?.textViewTitle?.visibility = View.VISIBLE
        binding?.textViewExerciseName?.visibility = View.INVISIBLE
        binding?.frameLayoutExerciseView?.visibility = View.INVISIBLE
        binding?.ivImage?.visibility = View.INVISIBLE
        binding?.textViewUpcomingLabel?.visibility = View.VISIBLE
        binding?.textViewUpcomingExerciseName?.visibility = View.VISIBLE

        if (restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }

        binding?.textViewUpcomingExerciseName?.text = exerciseList!![currentExercisePosition + 1].getName()

        setRestProgressBar()
    }

    private fun setRestProgressBar(){
        binding?.progressBar?.progress = restProgress
        restTimer = object : CountDownTimer(restTimerDuration*15000,1000){
            override fun onTick(millisUntilFinished: Long) {
                restProgress++
                binding?.progressBar?.progress = 15 - restProgress
                binding?.textViewTimer?.text = (15 - restProgress).toString()
            }
            override fun onFinish() {
                currentExercisePosition++

                exerciseList!![currentExercisePosition].setIsSelected(true)
                exerciseAdapter!!.notifyDataSetChanged()
                setupExerciseView()
            }
        }.start()
    }

    private fun setupExerciseView(){
        binding?.frameLayoutRestView?.visibility = View.INVISIBLE
        binding?.textViewTitle?.visibility = View.INVISIBLE
        binding?.textViewExerciseName?.visibility = View.VISIBLE
        binding?.frameLayoutExerciseView?.visibility = View.VISIBLE
        binding?.ivImage?.visibility = View.VISIBLE
        binding?.textViewUpcomingLabel?.visibility = View.INVISIBLE
        binding?.textViewUpcomingExerciseName?.visibility = View.INVISIBLE
        if (exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        speakOut(exerciseList!![currentExercisePosition].getName())

        binding?.ivImage?.setImageResource(exerciseList!![currentExercisePosition].getImage())
        binding?.textViewExerciseName?.text = exerciseList!![currentExercisePosition].getName()

        setExerciseProgressBar()
    }

    private fun setExerciseProgressBar(){
        binding?.progressBarExercise?.progress = exerciseProgress
        exerciseTimer = object : CountDownTimer(exerciseTimerDuration*35000,1000){
            override fun onTick(millisUntilFinished: Long) {
                exerciseProgress++
                binding?.progressBarExercise?.progress = 35 - exerciseProgress
                binding?.textViewTimerExercise?.text = (35 - exerciseProgress).toString()
            }
            override fun onFinish() {

                if (currentExercisePosition < exerciseList?.size!! - 1 ){
                    exerciseList!![currentExercisePosition].setIsSelected(false)
                    exerciseList!![currentExercisePosition].setIsCompleted(true)
                    exerciseAdapter!!.notifyDataSetChanged()
                    setupRestView()
                }else{
                finish()
                    val intent = Intent(this@ExerciseActivity,FinishActivity::class.java)
                    startActivity(intent)
            }}
        }.start()
    }
    override fun onDestroy() {
        super.onDestroy()
        if (restTimer != null){
            restTimer?.cancel()
            restProgress = 0
        }
        if (exerciseTimer != null){
            exerciseTimer?.cancel()
            exerciseProgress = 0
        }

        if (textToSpeech != null){
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }

        if (player != null){
            player!!.stop()
        }

        binding = null
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS){
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("TextToSpeech","The language specified is not supported!")
            }
        }else{
            Log.e("TextToSpeech","Initialization faled!")
        }
    }

    private fun speakOut(text: String){
        textToSpeech!!.speak(text,TextToSpeech.QUEUE_FLUSH,null,"")
    }
}