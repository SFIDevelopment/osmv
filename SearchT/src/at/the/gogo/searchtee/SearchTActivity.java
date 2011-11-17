package at.the.gogo.searchtee;



import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import at.the.gogo.searchtee.util.SpeakItOut;

public class SearchTActivity extends Activity implements
		TextToSpeech.OnInitListener {

	private static final int MY_TTS_CHECK_CODE = 1234;
	private static final int DELAY = 900;

	boolean wantToUseTTS = true;
	boolean canUseTTS = false;

	int images[] = {R.drawable.whamm, R.drawable.bam,R.drawable.pop,R.drawable.crash,R.drawable.aaaargh}; 
	
	ImageView smashImage;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		checkTTS();

		smashImage = (ImageView) findViewById(R.id.splash);
		
		ImageView image = (ImageView) findViewById(R.id.searchTee);
		image.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showSomething();
				smashImage.setVisibility(View.VISIBLE);
				
				speaksomething();

				ShowerTask task = new ShowerTask();
				task.execute((Void[]) null);

			}
		});

//		RelativeLayout rl = (RelativeLayout) findViewById(R.id.relativeLayout1);

//		View imageView = getLayoutInflater().inflate(R.layout.image_item, null);
		
		

//		smashImage.setImageResource(R.drawable.whamm);

//		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//				RelativeLayout.LayoutParams.MATCH_PARENT,
//				RelativeLayout.LayoutParams.MATCH_PARENT);
//		RelativeLayout.LayoutParams.WRAP_CONTENT,
//		RelativeLayout.LayoutParams.WRAP_CONTENT);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		
//		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//		
//		smashImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
		
//		rl.addView(imageView, layoutParams);
//		smashImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
//		smashImage.setScaleType(ImageView.ScaleType.MATRIX);
//
//		Matrix matrix =  new Matrix();
//		matrix.postScale(2, 2);
//		smashImage.setImageMatrix(matrix);
		
//		smashImage.setVisibility(View.GONE);

	}

	private void speekit(final String text) {

		if ((canUseTTS) && (SpeakItOut.getTts() != null)) {
			SpeakItOut.speak(text);
		}

	}

	private void speaksomething() {
		String[] texte = getResources().getStringArray(R.array.speakos);

		int index = (int) (Math.random() * texte.length);

		speekit(texte[index]);

	}

	private void showSomething()
	{
		int ix = (int) (Math.random() * images.length);
		
		smashImage.setImageResource(images[ix]);
	}
	
	private void checkTTS() {
		final Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_TTS_CHECK_CODE);
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		// System.out.println("Code:" + requestCode);
		// if (resultCode == RESULT_OK) {

		if (requestCode == MY_TTS_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				SpeakItOut.setTts(new TextToSpeech(this, this));
			} else {
				// missing data, install it
				final Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		} else {

			// NB: I only expect preferences to return here - so we kill and
			// restart

			finish();
			startActivity(new Intent(this, this.getClass()));
		}
		// }
	}

	// for TTS
	@Override
	public void onInit(final int status) {

		if (wantToUseTTS) // wanted & installed
			canUseTTS = true;
	}

	public class ShowerTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			try {
				Thread.sleep(DELAY);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return (Void) null;
		}

		@Override
		protected void onPostExecute(Void nix) {
			smashImage.setVisibility(View.GONE);
		}
	}

}