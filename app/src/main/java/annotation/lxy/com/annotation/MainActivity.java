package annotation.lxy.com.annotation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.lxy.process.anno.BindView;
import com.lxy.process.anno.TargetClass;


/**
 * @author a
 */
@TargetClass
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.bt)
    Button mBt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewMainActivity.bind(this);
        mBt.setText("bt");

    }
}
