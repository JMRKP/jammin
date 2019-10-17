package de.hdm_stuttgart.jammin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import de.hdm_stuttgart.jammin.repertoire.Repertoire;

// NOTICE: Repertoire tests are put here inside androidTest because AndroidStudio throws errors that method calls on JSONObjects aren't mocked if tests are run as local JUnit tests

public class RepertoireTest {
    Repertoire testRepertoire = new Repertoire();
    Repertoire testMergingRepertoire = new Repertoire();

    // Metallica - Moth Into Flame
    JSONObject songOneInApiStructure = new JSONObject("{\"wrapperType\":\"track\", \"kind\":\"song\", \"artistId\":3996865, \"collectionId\":1145498274, \"trackId\":1145498650, \"artistName\":\"Metallica\", \"collectionName\":\"Hardwired…To Self-Destruct (Deluxe)\", \"trackName\":\"Moth Into Flame\", \"collectionCensoredName\":\"Hardwired…To Self-Destruct (Deluxe)\", \"trackCensoredName\":\"Moth Into Flame\", \"artistViewUrl\":\"https://itunes.apple.com/us/artist/metallica/id3996865?uo=4\", \"collectionViewUrl\":\"https://itunes.apple.com/us/album/moth-into-flame/id1145498274?i=1145498650&uo=4\", \"trackViewUrl\":\"https://itunes.apple.com/us/album/moth-into-flame/id1145498274?i=1145498650&uo=4\", \"previewUrl\":\"http://audio.itunes.apple.com/apple-assets-us-std-000001/AudioPreview71/v4/8f/21/ea/8f21eaf6-238f-7e77-7bbc-0f17ac047d00/mzaf_6668992848175421017.plus.aac.p.m4a\", \"artworkUrl30\":\"http://is1.mzstatic.com/image/thumb/Music22/v4/74/14/2c/74142c23-9686-3cf2-a34b-8017f5873694/source/30x30bb.jpg\", \"artworkUrl60\":\"http://is1.mzstatic.com/image/thumb/Music22/v4/74/14/2c/74142c23-9686-3cf2-a34b-8017f5873694/source/60x60bb.jpg\", \"artworkUrl100\":\"http://is1.mzstatic.com/image/thumb/Music22/v4/74/14/2c/74142c23-9686-3cf2-a34b-8017f5873694/source/100x100bb.jpg\", \"collectionPrice\":14.99, \"trackPrice\":1.29, \"releaseDate\":\"2016-11-18T08:00:00Z\", \"collectionExplicitness\":\"explicit\", \"trackExplicitness\":\"notExplicit\", \"discCount\":3, \"discNumber\":1, \"trackCount\":6, \"trackNumber\":4, \"trackTimeMillis\":350644, \"country\":\"USA\", \"currency\":\"USD\", \"primaryGenreName\":\"Rock\", \"isStreamable\":true}");
    JSONObject songOneInInternalStructure = new JSONObject("{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Moth Into Flame\"},\"counter\":1}");

    // Metallica - Wherever I May Roam
    JSONObject songTwoInApiStructure = new JSONObject("{\"wrapperType\":\"track\", \"kind\":\"song\", \"artistId\":3996865, \"collectionId\":579372950, \"trackId\":579373083, \"artistName\":\"Metallica\", \"collectionName\":\"Metallica\", \"trackName\":\"Wherever I May Roam\", \"collectionCensoredName\":\"Metallica\", \"trackCensoredName\":\"Wherever I May Roam\", \"artistViewUrl\":\"https://itunes.apple.com/us/artist/metallica/id3996865?uo=4\", \"collectionViewUrl\":\"https://itunes.apple.com/us/album/wherever-i-may-roam/id579372950?i=579373083&uo=4\", \"trackViewUrl\":\"https://itunes.apple.com/us/album/wherever-i-may-roam/id579372950?i=579373083&uo=4\", \"previewUrl\":\"http://a920.phobos.apple.com/us/r30/Music7/v4/2b/ae/13/2bae1313-db61-66fc-d88a-900917e4df3f/mzaf_4434439105831171911.plus.aac.p.m4a\", \"artworkUrl30\":\"http://is5.mzstatic.com/image/thumb/Music/v4/0b/9c/d2/0b9cd2e7-6e76-8912-0357-14780cc2616a/source/30x30bb.jpg\", \"artworkUrl60\":\"http://is5.mzstatic.com/image/thumb/Music/v4/0b/9c/d2/0b9cd2e7-6e76-8912-0357-14780cc2616a/source/60x60bb.jpg\", \"artworkUrl100\":\"http://is5.mzstatic.com/image/thumb/Music/v4/0b/9c/d2/0b9cd2e7-6e76-8912-0357-14780cc2616a/source/100x100bb.jpg\", \"collectionPrice\":7.99, \"trackPrice\":1.29, \"releaseDate\":\"1991-08-12T07:00:00Z\", \"collectionExplicitness\":\"notExplicit\", \"trackExplicitness\":\"notExplicit\", \"discCount\":1, \"discNumber\":1, \"trackCount\":12, \"trackNumber\":5, \"trackTimeMillis\":404227, \"country\":\"USA\", \"currency\":\"USD\", \"primaryGenreName\":\"Rock\", \"isStreamable\":true}");
    JSONObject songTwoInInternalStructure = new JSONObject("{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Wherever I May Roam\"},\"counter\":1}");

    // Metallica - For Whom zhe Bell Tolls
    JSONObject songThreeInApiStructure = new JSONObject("{\"wrapperType\":\"track\", \"kind\":\"song\", \"artistId\":3996865, \"collectionId\":579148345, \"trackId\":579149036, \"artistName\":\"Metallica\", \"collectionName\":\"Ride the Lightning\", \"trackName\":\"For Whom the Bell Tolls\", \"collectionCensoredName\":\"Ride the Lightning\", \"trackCensoredName\":\"For Whom the Bell Tolls\", \"artistViewUrl\":\"https://itunes.apple.com/us/artist/metallica/id3996865?uo=4\", \"collectionViewUrl\":\"https://itunes.apple.com/us/album/for-whom-the-bell-tolls/id579148345?i=579149036&uo=4\", \"trackViewUrl\":\"https://itunes.apple.com/us/album/for-whom-the-bell-tolls/id579148345?i=579149036&uo=4\", \"previewUrl\":\"http://audio.itunes.apple.com/apple-assets-us-std-000001/AudioPreview60/v4/37/aa/f3/37aaf3b9-b59c-ab3d-616e-094ec6f17dcc/mzaf_7252537879900843424.plus.aac.p.m4a\", \"artworkUrl30\":\"http://is5.mzstatic.com/image/thumb/Music6/v4/86/87/0f/86870f81-8e08-2dc6-778b-f33e90ab5507/source/30x30bb.jpg\", \"artworkUrl60\":\"http://is5.mzstatic.com/image/thumb/Music6/v4/86/87/0f/86870f81-8e08-2dc6-778b-f33e90ab5507/source/60x60bb.jpg\", \"artworkUrl100\":\"http://is5.mzstatic.com/image/thumb/Music6/v4/86/87/0f/86870f81-8e08-2dc6-778b-f33e90ab5507/source/100x100bb.jpg\", \"collectionPrice\":7.99, \"trackPrice\":1.29, \"releaseDate\":\"1984-07-27T07:00:00Z\", \"collectionExplicitness\":\"notExplicit\", \"trackExplicitness\":\"notExplicit\", \"discCount\":1, \"discNumber\":1, \"trackCount\":8, \"trackNumber\":3, \"trackTimeMillis\":309973, \"country\":\"USA\", \"currency\":\"USD\", \"primaryGenreName\":\"Rock\", \"isStreamable\":true}");
    JSONObject songThreeInInternalStructure = new JSONObject("{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"For Whom the Bell Tolls\"},\"counter\":1}");

    public RepertoireTest() throws JSONException {
    }

    @Test
    public void addSong() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        testRepertoire.addSong(songTwoInApiStructure);

        JSONArray desiredResultRepertoireArray = new JSONArray("[{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Moth Into Flame\"},\"counter\":1},{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Wherever I May Roam\"},\"counter\":1}]");
        Assert.assertEquals(desiredResultRepertoireArray.toString(), testRepertoire.getRepertoire().toString());

        // Don't add songs twice
        testRepertoire.addSong(songOneInApiStructure);
        Assert.assertEquals(desiredResultRepertoireArray.toString(), testRepertoire.getRepertoire().toString());
    }

    @Test
    public void removeSong() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        testRepertoire.addSong(songTwoInApiStructure);
        testRepertoire.removeSong(songOneInInternalStructure);

        JSONArray desiredResultRepertoireArray = new JSONArray("[{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Wherever I May Roam\"},\"counter\":1}]");
        Assert.assertEquals(desiredResultRepertoireArray.toString(), testRepertoire.getRepertoire().toString());
    }

    @Test
    public void contains() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        testRepertoire.addSong(songTwoInApiStructure);

        boolean songOneContained = false,
                songTwoContained = false;

        // contains() returns the index, if contained, otherwise -1
        if (testRepertoire.contains(songOneInInternalStructure) > -1) {
            songOneContained = true;
        }
        if (testRepertoire.contains(songTwoInInternalStructure) > -1) {
            songTwoContained = true;
        }

        Assert.assertTrue(songOneContained);
        Assert.assertTrue(songTwoContained);
    }

    @Test
    public void getSong() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        String songOne = testRepertoire.getSong("Metallica - Moth Into Flame").toString();

        Assert.assertTrue(songOneInInternalStructure.toString().equals(songOne));
    }

    @Test
    public void mergeWithRepertoire() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        testRepertoire.addSong(songTwoInApiStructure);

        testMergingRepertoire.addSong(songTwoInApiStructure);
        testMergingRepertoire.addSong(songThreeInApiStructure);

        testMergingRepertoire.mergeWithRepertoire(testRepertoire);

        // desired result repertoire holds all three songs and the matching song has an increased counter
        JSONArray desiredResultRepertoireArray = new JSONArray("[{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Wherever I May Roam\"},\"counter\":2},{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"For Whom the Bell Tolls\"},\"counter\":1},{\"song\":{\"artistName\":\"Metallica\",\"trackName\":\"Moth Into Flame\"},\"counter\":1}]");
        Assert.assertEquals(desiredResultRepertoireArray.toString(), testMergingRepertoire.getRepertoire().toString());
    }

    @Test
    public void unmergeFromRepertoire() throws JSONException {
        testRepertoire.addSong(songOneInApiStructure);
        testRepertoire.addSong(songTwoInApiStructure);

        testMergingRepertoire.addSong(songTwoInApiStructure);
        testMergingRepertoire.addSong(songThreeInApiStructure);

        Repertoire testMergingRepertoireBeforeMerge = testMergingRepertoire;
        testMergingRepertoire.mergeWithRepertoire(testRepertoire);
        testMergingRepertoire.unmergeFromRepertoire(testRepertoire);

        // desired result repertoire now matches the original repertoire before the merge again
        Assert.assertEquals(testMergingRepertoire.toString(), testMergingRepertoireBeforeMerge.toString());
    }
}