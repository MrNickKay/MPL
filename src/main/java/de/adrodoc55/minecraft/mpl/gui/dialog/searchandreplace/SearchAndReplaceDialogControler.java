package de.adrodoc55.minecraft.mpl.gui.dialog.searchandreplace;

import java.awt.KeyboardFocusManager;
import java.awt.Window;

import javax.swing.WindowConstants;

import de.adrodoc55.minecraft.mpl.gui.dialog.searchandreplace.SearchAndReplaceDialogPM.Context;

/**
 * @author Adrodoc55
 */
public class SearchAndReplaceDialogControler {

  private final Context context;
  private SearchAndReplaceDialogPM pm;
  private SearchAndReplaceDialog view;

  public SearchAndReplaceDialogControler(Context context) {
    this.context = context;
  }

  public SearchAndReplaceDialogPM getPresentationModel() {
    if (pm == null) {
      pm = new SearchAndReplaceDialogPM(context);
    }
    return pm;
  }

  public SearchAndReplaceDialog getView() {
    if (view == null) {
      Window activeWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
      view = new SearchAndReplaceDialog(activeWindow);
      view.setPresentationModel(getPresentationModel());
      view.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }
    return view;
  }
}