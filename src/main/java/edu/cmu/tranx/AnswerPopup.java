package edu.cmu.tranx;

import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;



/**
 * Created by williamqian on 5/31/17.
 */
public class AnswerPopup<T> extends BaseListPopupStep<T> implements ListPopupStep<T>{
    public AnswerPopup( String title, T[] values) {
        super(title,values);


    }

    @Override
    public String getTextFor(T value) {
        return value.toString();

    }

    @Override
    public PopupStep onChosen(T value, final boolean finalChoice){
        //AccessText.chosen_query=value.query;
        return FINAL_CHOICE;


    }

    //public ListPopup createConfirmation(String title, String yesText, String noText, Runnable )
}
