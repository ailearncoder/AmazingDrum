package com.example.panpan.amazingdrum.sound;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import com.example.panpan.amazingdrum.store.AttachChordLoader;
import com.example.panpan.amazingdrum.store.DBManager;
import com.example.panpan.amazingdrum.util.Download;
import com.example.panpan.amazingdrum.util.GuitarSound2Piano;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * 和弦播放类
 */
public class ChordPlay {
    /****************************************************************************************************/
    public static final int SOUND_TYPE_GUITAR = 0;
    public static final int SOUND_TYPE_PAINO = 1;
    public static final int SOUND_TYPE_ELECTRIC_GUITAR = 2;
    public static final int SOUND_TYPE_BASS = 3;
    private static float volume2 = 1;// 用于指弹的音量
    private final int streamId2[] = new int[6];
    private final float volume[] = new float[]{1, 1, 1, 1, 1, 1};// 用于扫弦的音量
    private final boolean lowPitch[] = new boolean[]{false, false, false,
            true, true, true};
    private final boolean highPitch[] = new boolean[]{true, true, true,
            false, false, false};
    private final Context context;
    private final boolean isSettingFretNum = false;
    private final Random random = new Random();
    public int fretNum = 0;// 变调夹位置
    private HashMap<String, int[]> chordHashMap = new HashMap<String, int[]>();// 和弦名-->-->文件名
    private SparseBooleanArray allSoundName = new SparseBooleanArray();
    private SparseIntArray soundId = new SparseIntArray();
    private int streamId[] = new int[6];
    private int fingerStyleNum = 0;
    private int fingerStyleData[][] = new int[][]{{5}, {3}, {1}, {2}};
    private boolean isSetStyleData = false;
    private SoundPool soundPool;
    private boolean isLoadingFiles = false;
    private String gtpTxtPath;
    private AttachChordLoader chordLoader;
    private int soundType = SOUND_TYPE_GUITAR;
    private boolean isNeedGtp = true;
    private long previousPlayTime = 0;
    private boolean isPlay = false;// 播放
    private int sleepTime = 7;// 播放时间间隔
    private String presentPlayChordName;
    private int chordIndex = 0;
    private boolean string[];// 吉他扫弦琴弦

    public ChordPlay(Context context) {
        this.context = context;
    }

    public void addSoundName(int name) {
        if (allSoundName.indexOfKey(name) >= 0)
            return;
        allSoundName.put(name, true);
    }

    /**
     * 加载声音资源
     */
    private void loadSoundRes() {
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(
                        new AudioAttributes
                                .Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setFlags(AudioAttributes.FLAG_LOW_LATENCY)
                                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                                .build()
                )
                .setMaxStreams(10).build();
        int name, soundID;
        for (int i = 0; i < allSoundName.size(); i++) {
            name = allSoundName.keyAt(i);
            if (soundType == SOUND_TYPE_GUITAR)
                soundID = soundPool.load(Download.getResourcePath() + "/sound/"
                        + (name + fretNum) + ".ogg", 1);
            else if (soundType == SOUND_TYPE_PAINO)
                soundID = soundPool.load(
                        Download.getResourcePath()
                                + "/PianoSound/"
                                + GuitarSound2Piano.getPianoName(name / 100,
                                name % 100 + fretNum) + ".ogg", 1);
            else if (soundType == SOUND_TYPE_ELECTRIC_GUITAR)
                soundID = soundPool
                        .load(Download.getResourcePath()
                                        + "/ElectricGuitarSound/"
                                        + GuitarSound2Piano.getElectricGuitarName(name),
                                1);
            else
                soundID = soundPool.load(Download.getResourcePath() + "/BassSound/"
                        + (name + fretNum) + ".OGG", 1);
            soundId.put(name, soundID);
        }
    }

    /**
     * 重设数据
     */
    private void reset() {
        if (soundId != null) {
            soundId.clear();
        }
        if (chordHashMap != null) {
            chordHashMap.clear();
        }
        if (allSoundName != null) {
            allSoundName.clear();
        }
        if (streamId != null)
            for (int i = 0; i < streamId.length; i++) {
                streamId[i] = 0;
            }
        if (soundPool != null)
            soundPool.release();
        // fretNum = 0;// 变调夹位置
        fingerStyleNum = 0;
        isSetStyleData = false;
        isLoadingFiles = false;
        isNeedGtp = true;
        System.gc();
    }

    /**
     * 准备开始加载单个和弦文件
     */
    public void beginLoadSingleChordFile() {
        isLoadingFiles = true;
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        while (isPlay)// 等待播放结束
        {

        }
        reset();
    }

    /**
     * @param fretNum 变调夹所在品数 0 不夹变调夹 加载单个和弦文件
     * @throws Exception
     */
    public void loadSingleChordFile(int fretNum, String chord) throws Exception {
        if (!isNeedGtp)
            return;
        if (chordHashMap.containsKey(chord))// 去重
            return;
        this.fretNum = fretNum;
        String[] info = null;
        if (gtpTxtPath != null) {
            chordLoader = new AttachChordLoader();
            chordLoader.loadFromFile(gtpTxtPath);
            // DBManager.getInstance(context).add(gtpTxtPath);//不存入数据库
            gtpTxtPath = null;
        }
        String chord2;
        int end = chord.indexOf('-');
        if (end != -1)
            chord2 = chord.substring(0, end);
        else
            chord2 = chord;
        if (chordLoader != null && chordLoader.chordInfo.get(chord2) != null)
            info = new String[]{chordLoader.chordInfo.get(chord2)};
        if (info == null)
            info = DBManager.getInstance(context).queryInfo(null, chord2);
        if (info != null && info.length > 0) {
            String infos[] = info[0].split("_");
            if (infos.length != 0) {
                int name[] = new int[6];
                for (int k = 0; k < infos.length; k++) {
                    if (infos[k].charAt(1) != 'X') {
                        name[k] = Integer.valueOf(infos[k]);
                        addSoundName(name[k]);
                    } else {
                        name[k] = -1;
                    }
                }
                chordHashMap.put(chord, name);
            }
        } else {

            throw new Exception(chord + "和弦找不到！！！");
        }
    }

    /**
     * @param fretNum 变调夹所在品数 0 不夹变调夹 加载单个和弦文件
     * @param info    和弦所对应的数据
     * @throws Exception
     */
    public void loadSingleChordFile(int fretNum, String chord, String... info)
            throws Exception {
        this.fretNum = fretNum;
        if (info != null && info.length > 0) {
            String infos[] = info[0].split("_");
            if (infos.length != 0) {
                if (!chordHashMap.containsKey(chord)) {
                    int name[] = new int[6];
                    for (int k = 0; k < infos.length; k++) {
                        if (infos[k].charAt(1) != 'X') {
                            name[k] = Integer.valueOf(infos[k]);
                            addSoundName(name[k]);
                        } else {
                            name[k] = -1;
                        }
                    }
                    chordHashMap.put(chord, name);
                }
            }
        } else {
            throw new Exception(chord + "和弦找不到！！！");
        }
    }

    /**
     * 完成单个和弦文件的加载
     */
    public void endLoadSingleChordFile() {
        loadSoundRes();
        isLoadingFiles = false;
    }

    /**
     * 加载和弦文件
     *
     * @throws Exception
     */
    public void loadChordFiles(int fretNum, ArrayList<String> chords)
            throws Exception {
        this.fretNum = fretNum;
        beginLoadSingleChordFile();
        for (int i = 0; i < chords.size(); i++) {
            loadSingleChordFile(fretNum, chords.get(i));
        }
        endLoadSingleChordFile();
    }

    private void calculateVolume() {
        if (System.currentTimeMillis() - previousPlayTime < 500)// 60-80
        {
            volume[5] = 0.2f;// 60
            volume[4] = volume[5];
            volume[3] = 0.7f;// 70
            volume[2] = (30 + random.nextInt(31)) / 100.0f;// 50-80
            volume[1] = volume[2];
            volume[0] = (50 + random.nextInt(31)) / 100.0f;// 30-60
        } else {
            volume[5] = (20 + random.nextInt(31)) / 100.0f;
            volume[4] = volume[5];
            volume[3] = (50 + random.nextInt(31)) / 100.0f;// 70
            volume[2] = volume[3];
            volume[1] = volume[3];
            volume[0] = volume[3];
        }
        previousPlayTime = System.currentTimeMillis();
    }

    /**
     * 演奏 阻塞线程
     */
    public void play(String chordName, boolean isUp) {
        if (isLoadingFiles)
            return;
        isPlay = true;
        this.presentPlayChordName = chordName;
        chordIndex = 0;
        calculateVolume();
        int index2;
        int fileName;
        int soundID;
        for (int i = 0; i < 6; i++) {
            if (isUp)
                index2 = i;
            else
                index2 = 5 - i;
            if (string != null && !string[index2]) {
                chordIndex++;
                if (chordIndex > 5) {
                    chordIndex = 0;
                }
                continue;
            }
            int info[] = chordHashMap.get(presentPlayChordName);
            if (info == null) {
                continue;
            }
            fileName = info[index2];
            if (fileName != -1) {
                soundID = ChordPlay.this.soundId.get(fileName);
                streamId2[index2] = soundPool.play(soundID, volume[index2]
                        * volume2, volume[index2] * volume2, 1, 0, 1);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i < streamId.length; i++) {
            if (streamId[i] != 0)// 停止上一个弦的发音
                soundPool.stop(streamId[i]);
        }
        System.arraycopy(streamId2, 0, streamId, 0, streamId.length);// 保存当前
        isPlay = false;
    }

    /**
     * 静音
     */
    public void silence() {
        for (int i = 0; i < streamId.length; i++) {
            if (streamId[i] != 0)
                soundPool.stop(streamId[i]);
        }
    }

    /**
     * 设置当前和弦
     */
    public void setChordName(String chordName) {
        if (!isSetStyleData) {
            char temp = chordName.charAt(0);
            if (temp == 'A' || temp == 'B' || temp == 'C') {
                fingerStyleData[0][0] = 5;
            } else if (temp == 'D') {
                fingerStyleData[0][0] = 4;
            } else {
                fingerStyleData[0][0] = 6;
            }
        }
        this.presentPlayChordName = chordName;
        fingerStyleNum = 0;
    }

    /**
     * 指弹
     */
    public void fingerStylePlay() {
        if (isLoadingFiles || isSettingFretNum)
            return;
        int index;
        fingerStyleNum %= fingerStyleData.length;
        int info[] = chordHashMap.get(presentPlayChordName);
        int soundId = 0;
        if (info != null) {
            int fileName;
            for (int i = 0; i < fingerStyleData[fingerStyleNum].length; i++) {
                index = fingerStyleData[fingerStyleNum][i] - 1;
                fileName = info[index];
                if (fileName != -1) {
                    soundId = ChordPlay.this.soundId.get(fileName);
                    if (streamId[index] != 0)// 停止上一个弦的发音
                        soundPool.stop(streamId[index]);
                    streamId[index] = soundPool.play(soundId, volume2, volume2,
                            1, 0, 1);
                }
            }
            fingerStyleNum++;
        }
    }

    /**
     * 指弹
     */
    public void fingerStylePlay(int styleNum, int... string) {
        if (isLoadingFiles || isSettingFretNum)
            return;
        int info[] = chordHashMap.get(presentPlayChordName);
        int soundId = 0;
        if (info != null) {
            int fileName;
            switch (styleNum) {
                case 5:
                    for (int i = info.length - 1; i > -1; i--) {
                        fileName = info[i];
                        if (fileName > 0) {
                            soundId = ChordPlay.this.soundId.get(fileName);
                            streamId[i] = soundPool.play(soundId, volume2, volume2,
                                    1, 0, 1);
                            break;
                        }
                    }
                    break;
                case 6:
                    fileName = info[string[0]];
                    if (fileName > 0) {
                        soundId = ChordPlay.this.soundId.get(fileName);
                        streamId[string[0]] = soundPool.play(soundId, volume2,
                                volume2, 1, 0, 1);
                    }
                    break;
                case 7:
                    for (int i = 0; i < string.length; i++) {
                        fileName = info[string[i]];
                        if (fileName > 0) {
                            soundId = ChordPlay.this.soundId.get(fileName);
                            streamId[string[i]] = soundPool.play(soundId, volume2,
                                    volume2, 1, 0, 1);
                        }
                    }
                    break;
                case 8:
                    for (int i = 0; i < 6; i++) {
                        fileName = info[i];
                        if (fileName > 0) {
                            soundId = ChordPlay.this.soundId.get(fileName);
                            streamId[i] = soundPool.play(soundId, volume2, volume2,
                                    1, 0, 1);
                        }
                    }
                    break;
                case 9:

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 播放特定的音
     */
    public boolean play(char[] stringNum, int playStyle) {
        if (isLoadingFiles)
            return false;
        isPlay = true;
        if (soundType == SOUND_TYPE_ELECTRIC_GUITAR || soundType == SOUND_TYPE_BASS)
            playEleGuitar(stringNum);
        else
            playGuitar(stringNum, playStyle);
        isPlay = false;
        return true;
    }

    /**
     * 播放吉他
     */
    private void playGuitar(char[] stringNum, int playStyle) {
        switch (playStyle) {
            case 0:
                calculateVolume();
                for (int i = stringNum.length - 1; i > -1; i--) {
                    streamId2[i] = play(stringNum[i]);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < streamId.length; i++) {
                    if (streamId[i] != 0)// 停止上一个弦的发音
                        soundPool.stop(streamId[i]);
                }
                System.arraycopy(streamId2, 0, streamId, 0, streamId.length);// 保存当前
                break;
            case 1:
                calculateVolume();
                for (int i = 0; i < stringNum.length; i++) {
                    streamId2[i] = play(stringNum[i]);
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                for (int i = 0; i < streamId.length; i++) {
                    if (streamId[i] != 0)// 停止上一个弦的发音
                        soundPool.stop(streamId[i]);
                }
                System.arraycopy(streamId2, 0, streamId, 0, streamId.length);// 保存当前
                break;
            case 2:
            case 3:
            case 4:
                for (int i = stringNum.length - 1; i > -1; i--) {
                    streamId[i] = play(stringNum[i]);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 播放电吉他
     */
    private void playEleGuitar(char[] stringNum) {
        calculateVolume();
        for (int i = stringNum.length - 1; i > -1; i--) {
            streamId2[i] = play(stringNum[i]);
        }
        try {
            Thread.sleep(sleepTime * 6);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (int i = 0; i < streamId.length; i++) {
            if (streamId[i] != 0)// 停止上一个弦的发音
                soundPool.stop(streamId[i]);
        }
        System.arraycopy(streamId2, 0, streamId, 0, streamId.length);// 保存当前
    }

    /**
     * 播放和弦的指定弦
     */
    public void play(String chordName, char[] stringNum) {
        if (isLoadingFiles)
            return;
        isPlay = true;
        this.presentPlayChordName = chordName;
        int info[] = chordHashMap.get(presentPlayChordName);
        if (info == null && isNeedGtp) {
            return;
        }
        float volumeXX = 1;// 音量因子
        for (int i = 0; i < stringNum.length; i++) {
            if (0 < stringNum[i] && stringNum[i] < 7) {
                int fileName = info[stringNum[i] - 1];
                if (fileName != -1) {
                    volumeXX = 1;
                    int soundID = ChordPlay.this.soundId.get(fileName);
                    fileName /= 100;
                    if (fileName == 5)
                        volumeXX = 0.2f + random.nextFloat() * 0.3f;// 0.2-0.5
                    else if (fileName == 4)
                        volumeXX = 0.4f + random.nextFloat() * 0.4f;// 0.4-0.8
                    else if (fileName == 3)
                        volumeXX = 0.5f + random.nextFloat() * 0.3f;// 0.5-0.8
                    else if (fileName == 2)
                        volumeXX = 0.6f + random.nextFloat() * 0.3f;// 0.6-0.9
                    volumeXX = volume2 * volumeXX;
                    streamId2[i] = soundPool.play(soundID, volumeXX, volumeXX,
                            1, 0, 1);
                }
            } else {
                streamId[i] = play(stringNum[i]);
            }
        }
        isPlay = false;
    }

    /**
     * 电吉他播放
     */
    public void electricGuitarPlay(String chordName, char[] stringNum) {
        if (isLoadingFiles)
            return;
        isPlay = true;
        this.presentPlayChordName = chordName;
        int soundID = ChordPlay.this.soundId.get(stringNum[0]);
        streamId2[0] = soundPool.play(soundID, volume2, volume2, 1, 0, 1);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        soundPool.stop(streamId[0]);
        streamId[0] = streamId2[0];
        isPlay = false;
    }

    /**
     * 播放指定声音文件
     */
    private int play(int fileName) {
        if (fileName == 0)
            return 0;
        if (fileName > 699)
            soundPool.autoPause();
            //silence();
        else
            soundPool.autoResume();
        int streamID = 0;
        int soundID = ChordPlay.this.soundId.get(fileName);
        fileName /= 100;
        fileName--;
        fileName %= 6;
        streamID = soundPool.play(soundID, volume[fileName], volume[fileName],
                1, 0, 1);
        return streamID;
    }

    public SparseBooleanArray getAllSoundName() {
        return allSoundName;
    }

    public void release() {
        reset();
        soundId = null;
        chordHashMap = null;
        allSoundName = null;
        streamId = null;
        DBManager.getInstance(context).closeDB();
    }

    public float getVolume() {

        return volume2;
    }

    public static void setVolume(int volume) {

        ChordPlay.volume2 = volume / 100.0f;
    }

    public HashMap<String, int[]> getchordHashMap() {

        return chordHashMap;
    }

    public int getFretNum() {

        return fretNum;
    }

    /**
     * 设置变调夹位置
     */
    public void setFretNum(int fretNum) {
        if (fretNum < 0)
            fretNum = 0;
        if (fretNum > 12)
            fretNum = 11;
        this.fretNum = fretNum;
        int name;
        soundPool.release();
        soundPool = new SoundPool(allSoundName.size(),
                AudioManager.STREAM_MUSIC, 0);
        int soundID = 0;
        for (int i = 0; i < allSoundName.size(); i++) {
            name = allSoundName.keyAt(i);
            if (soundType == SOUND_TYPE_GUITAR)
                soundID = soundPool.load(Download.getResourcePath() + "/sound/"
                        + (name + fretNum) + ".OGG", 1);
            else if (soundType == SOUND_TYPE_PAINO)
                soundID = soundPool.load(
                        Download.getResourcePath()
                                + "/PianoSound/"
                                + GuitarSound2Piano.getPianoName(name / 100,
                                name % 100 + fretNum) + ".ogg", 1);
            else
                soundID = soundPool
                        .load(Download.getResourcePath()
                                        + "/ElectricGuitarSound/"
                                        + GuitarSound2Piano.getElectricGuitarName(name),
                                1);
            soundId.put(name, soundID);
        }
    }

    public void setFingerStyleData(int fingerStyleData[][]) {
        isSetStyleData = true;
        this.fingerStyleData = fingerStyleData;
    }

    public boolean raise() {
        if (fretNum + 1 > 6)
            return false;
        fretNum++;
        setFretNum(fretNum);
        return true;
    }

    public boolean reduce() {
        if (fretNum - 1 < 0)
            return false;
        fretNum--;
        setFretNum(fretNum);
        return true;
    }

    /**
     * 设置扫弦间隔
     */
    public void setSleepTime(int sleepTime) {
        this.sleepTime = sleepTime;
    }

    /**
     * 设置吉他琴弦
     */
    public void setGuitarString(boolean string[]) {
        this.string = string;
    }

    public void setPlayStyle(int styleNum) {
        switch (styleNum) {
            case 0:
                setGuitarString(null);
                setSleepTime(7);
                break;
            case 1:
                setGuitarString(lowPitch);
                setSleepTime(7);
                break;
            case 2:
                setGuitarString(highPitch);
                setSleepTime(7);
                break;
            case 3:
                setGuitarString(null);
                setSleepTime(5);
                break;
            case 4:
                setGuitarString(null);
                setSleepTime(15);
                break;
            default:
                break;
        }
    }

    /**
     * @return the gtpTxtPath
     */
    public String getGtpTxtPath() {
        return gtpTxtPath;
    }

    /**
     * @param gtpTxtPath the gtpTxtPath to set
     */
    public void setGtpTxtPath(String gtpTxtPath) {
        if (new File(gtpTxtPath).exists())
            this.gtpTxtPath = gtpTxtPath;
        else
            this.gtpTxtPath = null;
    }

    /**
     * @return the soundType
     */
    public int getSoundType() {
        return soundType;
    }

    /**
     * @param soundType the soundType to set
     */
    public void setSoundType(int soundType) {
        this.soundType = soundType;
    }

    /**
     * @return the isNeedGtp
     */
    public boolean isNeedGtp() {
        return isNeedGtp;
    }

    /**
     * @param isNeedGtp the isNeedGtp to set
     */
    public void setNeedGtp(boolean isNeedGtp) {
        this.isNeedGtp = isNeedGtp;
    }
}
