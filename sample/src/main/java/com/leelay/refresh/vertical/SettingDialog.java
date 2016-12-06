package com.leelay.refresh.vertical;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.leelay.refresh.vertical.config.Config;

/**
 * Created by leelay on 2016/12/6.
 */

public class SettingDialog {

    public static void showDialog(Context context, DialogInterface.OnDismissListener onDismissListener) {
        final View view = LayoutInflater.from(context).inflate(R.layout.layout_setting, null, false);
        setupView(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(context).setView(view);
        builder.setTitle("Setting");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ok(view);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton("reset", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Config.getInstance().reset();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if (onDismissListener != null)
            alertDialog.setOnDismissListener(onDismissListener);

    }

    private static void setupView(View alertDialog) {
        Config config = Config.getInstance();
        SeekBarLayout seekBarLayout01 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout01);
        seekBarLayout01.setRange(0.2f, 0.8f);
        seekBarLayout01.setProgress(config.dragRate);
        seekBarLayout01.setTitle("阻力系数");

        SeekBarLayout seekBarLayout02 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout02);
        seekBarLayout02.setRange(1.0f, 3.0f);
        seekBarLayout02.setProgress(config.ratioOfHeaderHeightToReach);
        seekBarLayout02.setTitle("最大高度比例");

        SeekBarLayout seekBarLayout03 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout03);
        seekBarLayout03.setRange(100, 5000);
        seekBarLayout03.setProgress(config.completeStickDuration);
        seekBarLayout03.setTitle("头部滞留时长");

        SeekBarLayout seekBarLayout04 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout04);
        seekBarLayout04.setRange(100, 5000);
        seekBarLayout04.setProgress(config.autoRefreshDuration);
        seekBarLayout04.setTitle("自动下拉时长");

        SeekBarLayout seekBarLayout05 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout05);
        seekBarLayout05.setRange(100, 5000);
        seekBarLayout05.setProgress(config.toRetainDuration);
        seekBarLayout05.setTitle("回到刷新时长");

        SeekBarLayout seekBarLayout06 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout06);
        seekBarLayout06.setRange(100, 5000);
        seekBarLayout06.setProgress(config.toStartDuration);
        seekBarLayout06.setTitle("回到顶部时长");

    }

    private static void ok(View alertDialog) {
        Config config = Config.getInstance();
        SeekBarLayout seekBarLayout01 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout01);
        config.dragRate = Float.parseFloat(seekBarLayout01.getProcess());

        SeekBarLayout seekBarLayout02 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout02);
        config.ratioOfHeaderHeightToReach = Float.parseFloat(seekBarLayout02.getProcess());

        SeekBarLayout seekBarLayout03 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout03);
        config.completeStickDuration = Integer.parseInt(seekBarLayout03.getProcess());
        SeekBarLayout seekBarLayout04 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout04);
        config.autoRefreshDuration = Integer.parseInt(seekBarLayout04.getProcess());

        SeekBarLayout seekBarLayout05 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout05);
        config.toRetainDuration = Integer.parseInt(seekBarLayout05.getProcess());

        SeekBarLayout seekBarLayout06 = (SeekBarLayout) alertDialog.findViewById(R.id.seekbar_layout06);
        config.toStartDuration = Integer.parseInt(seekBarLayout06.getProcess());
    }

}
