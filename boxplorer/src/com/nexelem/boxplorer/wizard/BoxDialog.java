package com.nexelem.boxplorer.wizard;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nexelem.boxplorer.Fonts;
import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.activity.NfcWriter;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.utils.NfcUtils;
import com.nexelem.boxplorer.utils.ObjectKeeper;
import com.nexelem.boxplorer.utils.QrUtils;

@SuppressLint("ValidFragment")
public class BoxDialog extends DialogFragment {

	private Box box;
	private boolean isQr;
	private boolean isNfc;

	private int step;
	private ViewFlipper flipper;
	private boolean update = false;
	private final View[] steps = new View[3];

	public BoxDialog() {
	}

	public BoxDialog(Box box) {
		this.box = box;
		this.update = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("name", this.box.getName());
		outState.putString("localization", this.box.getLocation());
		outState.putBoolean("isQr", this.isQr);
		outState.putBoolean("isNfc", this.isNfc);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

		View view = inflater.inflate(R.layout.wizard, container, false);
		View step1 = inflater.inflate(R.layout.wizard_box_1, null);
		View step2 = inflater.inflate(R.layout.wizard_box_2, null);
		View step3 = inflater.inflate(R.layout.wizard_box_3, null);

		this.steps[0] = view.findViewById(R.id.wizard_step1);
		this.steps[1] = view.findViewById(R.id.wizard_step2);
		this.steps[2] = view.findViewById(R.id.wizard_step3);

		this.step = 0;

		this.flipper = (ViewFlipper) view.findViewById(R.id.flipper);

		this.flipper.addView(step1, 0);
		this.flipper.addView(step2, 1);
		this.flipper.addView(step3, 2);

		this.flipper.setInAnimation(AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.slide_in_left));
		this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.slide_out_right));

		// Title
		TextView title = (TextView) view.findViewById(R.id.wizard_title);
		title.setTypeface(Fonts.REGULAR_FONT);
		title.setText(this.update ? R.string.box_edit : R.string.box_add);

		// Buttons
		final Button next = (Button) view.findViewById(R.id.button_next);
		final Button back = (Button) view.findViewById(R.id.button_back);

		next.setText(R.string.finish);
		back.setText(R.string.cancel);
		next.setTypeface(Fonts.REGULAR_FONT);
		back.setTypeface(Fonts.REGULAR_FONT);

		// Step 1
		final EditText name = (EditText) step1.findViewById(R.id.box_add_name);
		final EditText localization = (EditText) step1.findViewById(R.id.box_add_localization);
		final CheckBox isQr = (CheckBox) step1.findViewById(R.id.box_add_qr);
		final CheckBox isNfc = (CheckBox) step1.findViewById(R.id.box_add_nfc);

		TextView boxName = (TextView) view.findViewById(R.id.box_name);
		TextView boxLocation = (TextView) view.findViewById(R.id.box_location);
		boxName.setTypeface(Fonts.LIGHT_FONT);
		boxLocation.setTypeface(Fonts.LIGHT_FONT);
		isQr.setTypeface(Fonts.LIGHT_FONT);
		isNfc.setTypeface(Fonts.LIGHT_FONT);

		if (this.box != null) {
			name.setText(this.box.getName());
			localization.setText(this.box.getLocation());
		} else {
			this.box = new Box();
		}

		isQr.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				if (checked) {
					next.setText(R.string.next);
				} else if (!isNfc.isChecked()) {
					next.setText(R.string.finish);
				}
			}
		});

		isNfc.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				if (checked) {
					next.setText(R.string.next);
				} else if (!isQr.isChecked()) {
					next.setText(R.string.finish);
				}
			}
		});

		// Step 2
		final ImageView image = (ImageView) step2.findViewById(R.id.box_add_generated_qr);
		TextView qrTitle = (TextView) step2.findViewById(R.id.box_qr_title);
		TextView qrText = (TextView) step2.findViewById(R.id.box_qr_text);
		qrTitle.setTypeface(Fonts.LIGHT_FONT);
		qrText.setTypeface(Fonts.LIGHT_FONT);

		if (savedInstanceState != null) {
			name.setText(savedInstanceState.getString("name"));
			localization.setText(savedInstanceState.getString("localization"));
			isQr.setChecked(savedInstanceState.getBoolean("isQr"));
			isNfc.setChecked(savedInstanceState.getBoolean("isNfc"));
		}

		// Step 3
		TextView nfcTitle = (TextView) step3.findViewById(R.id.box_nfc_title);
		TextView nfcText = (TextView) step3.findViewById(R.id.box_nfc_text);
		nfcTitle.setTypeface(Fonts.LIGHT_FONT);
		nfcText.setTypeface(Fonts.LIGHT_FONT);

		next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				switch (BoxDialog.this.step) {
				case 0:
					if (name.getEditableText().length() == 0) {
						Toast.makeText(BoxDialog.this.getActivity(), BoxDialog.this.getString(R.string.box_name_empty), Toast.LENGTH_SHORT).show();
						return;
					}

					BoxDialog.this.box.setName(name.getEditableText().toString());
					BoxDialog.this.box.setLocation(localization.getEditableText().toString());
					BoxDialog.this.isQr = isQr.isChecked();
					BoxDialog.this.isNfc = isNfc.isChecked();

					try {
						if (BoxDialog.this.update) {
							ObjectKeeper.getInstance().getBoxService().update(BoxDialog.this.box);
						} else {
							ObjectKeeper.getInstance().getBoxService().create(BoxDialog.this.box);
						}
						ObjectKeeper.getInstance().getListAdapter().updateListAdapterData();
						ObjectKeeper.getInstance().getListAdapter().notifyDataSetChanged();
					} catch (BusinessException e) {
						e.printStackTrace();
					}

					if (isQr.isChecked()) {
						BoxDialog.this.setStep(1);

						try {
							image.setImageBitmap(QrUtils.generateQRCode(BoxDialog.this.getActivity(), BoxDialog.this.box.getId().toString()));
						} catch (BusinessException e) {
							Toast.makeText(BoxDialog.this.getActivity().getApplicationContext(), R.string.qr_generation_failed, Toast.LENGTH_SHORT).show();
						}
						next.setText(isNfc.isChecked() ? R.string.next : R.string.finish);

					} else if (isNfc.isChecked()) {
						BoxDialog.this.setStep(2);
						next.setText(R.string.finish);
					} else {
						BoxDialog.this.getDialog().cancel();
					}
					break;

				case 1:
					if (isNfc.isChecked()) {
						BoxDialog.this.setStep(2);
						next.setText(R.string.finish);
					} else {
						BoxDialog.this.getDialog().cancel();
					}
					break;
				case 2:
					BoxDialog.this.getDialog().cancel();
				}
			}
		});

		back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (BoxDialog.this.step) {
				case 0:
					BoxDialog.this.getDialog().cancel();
					break;
				case 1:
					BoxDialog.this.setStep(0);
					next.setText(R.string.next);
					break;
				case 2:
					if (isQr.isChecked()) {
						BoxDialog.this.setStep(1);
						next.setText(R.string.next);
					} else {
						BoxDialog.this.setStep(0);
						next.setText(R.string.next);
					}
					break;
				}
			}
		});

		return view;
	}

	private void setStep(int i) {
		this.flipper.setDisplayedChild(i);
		this.step = i;

		if (i == 2) {
			if (NfcUtils.isNfcAvailable(this.getActivity().getApplicationContext())) {
				this.writeNfcTag();
			}
		} else {
			TextView msg = (TextView) this.steps[2].findViewById(R.id.box_nfc_text);
			if (msg != null) {
				msg.setText(R.string.tap_nfc_write);
			}
		}

		for (int j = 0; j < 3; j++) {
			this.steps[j].setBackgroundResource(j <= i ? R.color.main_color : R.color.dark_gray);
		}

	}

	private void writeNfcTag() {
		Intent intent = new Intent(this.getActivity(), NfcWriter.class);
		intent.putExtra(Intent.EXTRA_UID, this.box.getId().toString());
		this.startActivityForResult(intent, NfcWriter.ACTIVITY_WRITE_NFC);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == NfcWriter.ACTIVITY_WRITE_NFC) {
			int returnMsg = data.getIntExtra(Intent.EXTRA_TEXT, 0);
			TextView msg = (TextView) this.steps[2].findViewById(R.id.box_nfc_text);
			if (msg != null) {
				msg.setText(returnMsg);
			}

		}
	}

}
