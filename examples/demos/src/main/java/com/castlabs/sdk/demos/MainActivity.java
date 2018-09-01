package com.castlabs.sdk.demos;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.castlabs.android.SdkConsts;
import com.castlabs.android.drm.Drm;
import com.castlabs.android.drm.DrmConfiguration;
import com.castlabs.android.drm.DrmTodayConfiguration;
import com.castlabs.android.network.NetworkConfiguration;
import com.castlabs.android.network.RetryConfiguration;
import com.castlabs.android.player.AbrConfiguration;
import com.castlabs.android.player.BufferConfiguration;
import com.castlabs.android.player.LiveConfiguration;
import com.castlabs.sdk.ima.ImaAdRequest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static Demo[] DEMOS = new Demo[]{
            new Demo("VINH LONG 1 hls", "hls vinhlong1",
                    SimplePlaybackDemo.class,
                    new BundleBuilder()
                            //.put(SdkConsts.INTENT_URL, "http://123.30.233.190/cliptv_vod_v2/_definst_/amlst:cmc/media1/0/0/3/99305.mp4/playlist.m3u8") // 4k vod
                            .put(SdkConsts.INTENT_URL, "http://157.119.246.23/live_huyvq/amlst:vinhlong1hd/playlist.m3u8")
                            //.put(SdkConsts.INTENT_VIDEO_CODEC_FILTER, SdkConsts.VIDEO_CODEC_FILTER_CAPS)
                            .put(SdkConsts.INTENT_VIDEO_SIZE_FILTER, SdkConsts.VIDEO_SIZE_FILTER_NONE)
                            .put(SdkConsts.INTENT_HD_CONTENT_FILTER,
                                    SdkConsts.ALLOW_HD_CLEAR_CONTENT
                                            | SdkConsts.ALLOW_HD_DRM_SOFTWARE
                                            | SdkConsts.ALLOW_HD_DRM_ROOT_OF_TRUST
                                            | SdkConsts.ALLOW_HD_DRM_SECURE_MEDIA_PATH)
                            .put(SdkConsts.INTENT_LIVE_CONFIGURATION, new LiveConfiguration.Builder()
                                    .hlsLiveTailSegmentIndex(10)
                                    .hlsPlaylistUpdateTargetDurationCoefficient(0.2f, true)
                                    .get())
                            .put(SdkConsts.INTENT_ABR_CONFIGURATION, new AbrConfiguration.Builder()
                                    .bandwidthFraction(0.5f)
                                    .safeBufferSize(2000000L, TimeUnit.NANOSECONDS)
                                    .minDurationForQualityIncrease(15000000L, TimeUnit.NANOSECONDS)
                                    .downloadTimeFactor(1.5f).get())
                            .put(SdkConsts.INTENT_BUFFER_CONFIGURATION, new BufferConfiguration.Builder()
                                    .bufferSizeBytes(32 * 1024 * 1024)
                                    .lowMediaTime(20, TimeUnit.SECONDS)
                                    .highMediaTime(60, TimeUnit.SECONDS)
                                    //.minPlaybackStart(10, TimeUnit.SECONDS)
                                    //.minRebufferStart(10, TimeUnit.SECONDS)

                                    .prioritizeTimeOverSizeThresholds(true)
                                    //.drainWhileCharging(true)
                                    .backBufferDuration(1, TimeUnit.SECONDS)
                                    .get())
                            .put(SdkConsts.INTENT_NETWORK_CONFIGURATION, new NetworkConfiguration.Builder()
                                    .connectionTimeoutMs(5000)
                                    .readTimeoutMs(8000)
                                    .manifestConnectionTimeoutMs(3000)
                                    .manifestReadTimeoutMs(5000)
                                    .segmentsConnectionTimeoutMs(8000)
                                    .segmentReadTimeoutMs(10000)
                                    .retryConfiguration(new RetryConfiguration.Builder()
                                            .maxAttempts(5)
                                            .baseDelayMs(500)
                                            .get())
                                    .manifestRetryConfiguration(new RetryConfiguration.Builder()
                                            .maxAttempts(2)
                                            .baseDelayMs(1000)
                                            .get())
                                    .segmentsRetryConfiguration(new RetryConfiguration.Builder()
                                            .maxAttempts(4)
                                            .baseDelayMs(800)
                                            .get())
                                    .get())
                            .get()),
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("", "0000000000000000000000000");
        setContentView(R.layout.activity_main);

        ListView demosList = (ListView) findViewById(R.id.demos_list);
        List<Demo> demos = Arrays.asList(DEMOS);
        final ArrayAdapter<Demo> demoAdapter = new ArrayAdapter<Demo>(this, R.layout.demo_list_item, demos) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView == null ? getLayoutInflater().inflate(R.layout.demo_list_item, parent, false) : convertView;
                Demo item = getItem(position);
                ((TextView) view.findViewById(R.id.title)).setText(item.name);
                ((TextView) view.findViewById(R.id.description)).setText(item.description);
                return view;
            }
        };

        demosList.setAdapter(demoAdapter);
        demosList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                demoAdapter.getItem(position).start(MainActivity.this);
            }
        });
    }


    static class Demo {
        public final String name;
        public final String description;
        private final Class<? extends Activity> activityClass;
        private final Bundle bundle;

        Demo(String name, String description, Class<? extends Activity> activityClass, Bundle bundle) {
            this.name = name;
            this.description = description;
            this.activityClass = activityClass;
            this.bundle = bundle;
        }

        void start(Context context) {
            context.startActivity(new Intent(context, activityClass).putExtras(bundle));
        }
    }


    static class BundleBuilder {
        private final Bundle bundle;

        BundleBuilder() {
            this.bundle = new Bundle();
        }

        BundleBuilder put(String key, String value) {
            bundle.putString(key, value);
            return this;
        }

        BundleBuilder put(String key, int value) {
            bundle.putInt(key, value);
            return this;
        }

        BundleBuilder put(String key, boolean value) {
            bundle.putBoolean(key, value);
            return this;
        }

        BundleBuilder put(String key, Parcelable value) {
            bundle.putParcelable(key, value);
            return this;
        }

        Bundle get() {
            return bundle;
        }
    }
}
