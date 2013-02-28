package com.nexelem.boxplorer.wizard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.nexelem.boxplorer.R;
import com.nexelem.boxplorer.db.BusinessException;
import com.nexelem.boxplorer.model.Box;
import com.nexelem.boxplorer.utils.NfcWriterActivity;
import com.nexelem.boxplorer.utils.ObjectKeeper;
import com.nexelem.boxplorer.utils.QRCodeUtils;

public class BoxDialog extends DialogFragment {

	private Box box;
	private boolean isQr;
	private boolean isNfc;

	private int step;
	private ViewFlipper flipper;
	private boolean update = false;

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
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		LayoutInflater inflater = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.wizard, null);

		this.flipper = (ViewFlipper) view.findViewById(R.id.flipper);

		View step1 = inflater.inflate(R.layout.wizard_box_1, null);
		View step2 = inflater.inflate(R.layout.wizard_box_2, null);
		View step3 = inflater.inflate(R.layout.wizard_box_3, null);

		this.flipper.addView(step1, 0);
		this.flipper.addView(step2, 1);
		this.flipper.addView(step3, 2);

		this.flipper.setInAnimation(AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.slide_in_left));
		this.flipper.setOutAnimation(AnimationUtils.loadAnimation(this.getActivity(), android.R.anim.slide_out_right));

		this.step = 0;

		// Step 1
		final EditText name = (EditText) step1.findViewById(R.id.box_add_name);
		final EditText localization = (EditText) step1.findViewById(R.id.box_add_localization);
		final CheckBox isQr = (CheckBox) step1.findViewById(R.id.box_add_qr);
		final CheckBox isNfc = (CheckBox) step1.findViewById(R.id.box_add_nfc);

		if (this.box != null) {
			name.setText(this.box.getName());
			localization.setText(this.box.getLocation());
		} else {
			this.box = new Box();
		}
		// Step 2
		final ImageView image = (ImageView) step2.findViewById(R.id.box_add_generated_qr);

		if (savedInstanceState != null) {
			name.setText(savedInstanceState.getString("name"));
			localization.setText(savedInstanceState.getString("localization"));
			isQr.setChecked(savedInstanceState.getBoolean("isQr"));
			isNfc.setChecked(savedInstanceState.getBoolean("isNfc"));
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());

		builder.setTitle("Add box");
		builder.setPositiveButton("Create", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.setNegativeButton("Cancel", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		builder.setView(view);

		final AlertDialog dialog = builder.create();
		dialog.show();

		Button positive = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		positive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				switch (BoxDialog.this.step) {
				case 0:
					if (name.getEditableText().length() == 0) {
						Toast.makeText(BoxDialog.this.getActivity(), "Insert box name", Toast.LENGTH_LONG).show();
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
						image.setImageBitmap(QRCodeUtils.generateQRCode(BoxDialog.this.getActivity(), BoxDialog.this.box.getId().toString()));
					} else if (isNfc.isChecked()) {
						BoxDialog.this.setStep(2);
					} else {
						dialog.cancel();
					}
					break;

				case 1:
					if (isNfc.isChecked()) {
						BoxDialog.this.setStep(2);
					} else {
						dialog.cancel();
					}
					break;
				case 2:
					dialog.cancel();
				}
			}
		});

		Button negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		negative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				switch (BoxDialog.this.step) {
				case 0:
					dialog.cancel();
					break;
				case 1:
					BoxDialog.this.setStep(0);
					break;
				case 2:
					if (isQr.isChecked()) {
						BoxDialog.this.setStep(1);
					} else {
						BoxDialog.this.setStep(0);
					}
					break;
				}
			}
		});

		return dialog;
	}

	private void setStep(int i) {
		this.flipper.setDisplayedChild(i);
		this.step = i;

		if (i == 2) {
			this.writeNfcTag();
		}
	}

	private void writeNfcTag() {
		Intent intent = new Intent(this.getActivity(), NfcWriterActivity.class);
		intent.putExtra(Intent.EXTRA_UID, this.box.getId().toString());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		this.startActivityForResult(intent, 0);
	}

}
