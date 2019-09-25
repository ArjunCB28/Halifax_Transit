package com.example.assignment3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.HashSet;

public class MultiSelectDialog extends DialogFragment {
    ArrayList<Integer> selectedItems;
    private String[] busNos;

    public MultiSelectDialog(String[] busNos) {
        this.busNos = busNos;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String[] items = busNos;
        selectedItems = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select bus route").setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                if (isChecked) {
                    // if the user checked the item, add it to the selected items
                    selectedItems.add(item);
                } else if (selectedItems.contains(item)) {
                    // else if the item is already in the array, remove it
                    selectedItems.remove(Integer.valueOf(item));
                }

            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                MapsActivity.filterCalled = false;
                if (selectedItems.size() > 0) {
                    MapsActivity.filterCalled = true;
                    HashSet<String> selectedBusNos = new HashSet<>();
                    for (int i = 0; i < selectedItems.size(); i++) {
                        System.out.println(selectedItems.get(i));
                        selectedBusNos.add(items[selectedItems.get(i)]);
                    }
                    MapsActivity.filteredBusNo = selectedBusNos;
                    new VehiclePositionReader().execute();
                }
            }
        }).setNeutralButton("Select All",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MapsActivity.filterCalled = true;
                        HashSet<String> selectedBusNos = new HashSet<>();
                        for (int i = 0; i < items.length ; i++) {
                            selectedBusNos.add(items[i]);
                        }
                        MapsActivity.filteredBusNo = selectedBusNos;
                        new VehiclePositionReader().execute();
                    }
                });

        return builder.create();
    }
}