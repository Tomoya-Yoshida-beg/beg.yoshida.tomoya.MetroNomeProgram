package beg.yoshida.metronomeprogram;

import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity{


    private SoundPool soundPool;
    private int soundId;
    private ScheduledThreadPoolExecutor scheduler;
    private ScheduledFuture<?> scheduledTask;
    private boolean isRunning = false;

    int userBpm;
    int numerator;
    int denominator;
    int repeatBar;
    int changeAmount;
    int upAndDown;
    int endBpm;
    int repeatTimesAll;

    int HowManySounded;
    int HowPastBar;

    ArrayList<Integer> bpms;

    long bpmToMs;

    NumberPicker pickBpm;
    NumberPicker pickBeatNumerator;
    NumberPicker pickBeatDenominator;
    NumberPicker pickRepeatBar;
    NumberPicker pickChangeAmount;
    NumberPicker pickUpAndDown;
    NumberPicker pickEndBpm;
    NumberPicker pickRepeatTimesAll;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pickBpm             = findViewById(R.id.pickBpm);
        pickBeatNumerator   = findViewById(R.id.pickBeatNumerator);
        pickBeatDenominator = findViewById(R.id.pickBeatDenominator);
        pickRepeatBar       = findViewById(R.id.pickRepeatBar);
        pickChangeAmount = findViewById(R.id.pickChangeAmountBpm);
        pickUpAndDown       = findViewById(R.id.pickUpAndDown);
        pickEndBpm          = findViewById(R.id.pickEndBpm);
        pickRepeatTimesAll  = findViewById(R.id.pickRepeatTimesAll);

        // SoundPool の初期化
        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .build();
        soundId = soundPool.load(this, R.raw.click, 1);

        List<NumberPickerConfig> configs = Arrays.asList(
                new NumberPickerConfig.Builder(R.id.pickBpm)
                        .range(40,300,120).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickBeatNumerator)
                        .range(2,8,4).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickBeatDenominator)
                        .range(2,19,4).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickRepeatBar)
                        .range(2,40,4).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickChangeAmountBpm)
                        .range(1,40,8).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickUpAndDown)
                        .labels(new String[]{"下げる","上げる"},0).build(),
                new NumberPickerConfig.Builder(R.id.pickEndBpm)
                        .range(40,300,90).wrap(true).build(),
                new NumberPickerConfig.Builder(R.id.pickRepeatTimesAll)
                        .range(1,40,4).wrap(true).build()

        );

        NumberPickerInitializer initializer = new NumberPickerInitializer();
        initializer.initNumberPicker(this,configs);


        Button btn = findViewById(R.id.startButton);
        btn.setOnClickListener(v -> toggleMetronome());


    }



    private void toggleMetronome() {
        if (isRunning) {
            stopMetronome();
        } else {
            startMetronome();
        }
    }

    void getValue(){
        userBpm = pickBpm.getValue();
        numerator = pickBeatNumerator.getValue();
        denominator = pickBeatDenominator.getValue();
        repeatBar = pickRepeatBar.getValue();
        changeAmount = pickChangeAmount.getValue();
        upAndDown = pickUpAndDown.getValue();
        endBpm = pickEndBpm.getValue();
        repeatTimesAll = pickRepeatTimesAll.getValue();
    }


    //bpMのスケジュールを配列として格納する。
    ArrayList<Integer> makeSequenceBpm() {

        bpms = new ArrayList<>();

        if (upAndDown == 0) {
            for (int i = 0; userBpm >= endBpm; userBpm -= changeAmount) {
                bpms.add(userBpm);
            }
        } else {
            for (int i = 0; userBpm <= endBpm; userBpm += changeAmount) {
                bpms.add(userBpm);
            }
        }
        return bpms;
    }

    //少し整理。スタート押す->判定入る->スケジュール組む->鳴る->同じBPMで鳴り終わったらもっかいスケジュール組む

    //scheduleの再生成。コイツが最初から最後まで生成され続ける。
    void scheduleNextTick(){
        scheduledTask = scheduler.schedule(tick,60000/bpms.get(HowPastBar),TimeUnit.MILLISECONDS);
    }

    //いまいちコレの書き方慣れない。要勉強。
    //一回鳴る時の判定
    Runnable tick = new Runnable() {
        @Override
        public void run() {

            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);

            if (HowManySounded <= numerator * repeatBar) {
                HowManySounded++;
            } else {
                HowPastBar++;
                if (HowPastBar >= bpms.size()){
                    stopMetronome();
                    return;
                }
            }

            scheduleNextTick();
        }
    };





    private void startMetronome() {
        getValue();
        makeSequenceBpm();

        scheduler = new ScheduledThreadPoolExecutor(1);
        //cancel()したタスクをキューから破棄をするよう設定
        scheduler.setRemoveOnCancelPolicy(true);

        HowManySounded = 0;
        HowPastBar = 0;
        isRunning = true;

        //初回呼び出し
        scheduleNextTick();

    }

    private void stopMetronome() {
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        isRunning = false;
        scheduler.shutdownNow();

        HowManySounded = 0;
        HowPastBar = 0;
    }

    //ライフサイクル
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMetronome();
        soundPool.release();
    }
}
