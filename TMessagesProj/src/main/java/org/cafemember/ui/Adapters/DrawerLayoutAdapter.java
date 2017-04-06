/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.cafemember.ui.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.cafemember.messenger.AndroidUtilities;
import org.cafemember.messenger.LocaleController;
import org.cafemember.messenger.MessagesController;
import org.cafemember.messenger.R;
import org.cafemember.messenger.UserConfig;
import org.cafemember.messenger.mytg.FontManager;
import org.cafemember.messenger.mytg.util.Defaults;
import org.cafemember.ui.Cells.DrawerActionCell;
import org.cafemember.ui.Cells.DividerCell;
import org.cafemember.ui.Cells.EmptyCell;
import org.cafemember.ui.Cells.DrawerProfileCell;
import org.cafemember.ui.Cells.TextCheckCell;

public class DrawerLayoutAdapter extends BaseAdapter {

    private Context mContext;

    public DrawerLayoutAdapter(Context context) {
        mContext = context;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return !(i == 0 || i == 1);
    }

    @Override
    public int getCount() {
        return UserConfig.isClientActivated() ? 11 : 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        int type = getItemViewType(i);
        if (type == 0) {
            if (view == null) {
                view = new DrawerProfileCell(mContext);
            }
            ((DrawerProfileCell) view).setUser(MessagesController.getInstance().getUser(UserConfig.getClientUserId()));
        } else if (type == 1) {
            if (view == null) {
                view = new EmptyCell(mContext, AndroidUtilities.dp(8));
            }
        } else if (type == 2) {
            if (view == null) {
                view = new DividerCell(mContext);
            }
        /*} else if (type == 10) {
            if (view == null) {
                view = new DividerCell(mContext);
            }*/
        } else if (type == 3) {
            if (view == null) {
                if(i == 2){
                    //view = new TextCheckCell(mContext);
                    view = new DrawerActionCell(mContext);
                }
                else {
                    view = new DrawerActionCell(mContext);
                }
            }
            try {

                /*if (i == 2) {
                    TextCheckCell actionCell = (TextCheckCell) view;
                    boolean isEnabled = Defaults.getInstance().openOnJoin();
                    actionCell.setTextAndCheck(LocaleController.getString("MenuJoinCheck", R.string.MenuJoinCheck), isEnabled, false);
                    FontManager.instance().setTypefaceImmediate(view);
                    return view;
                } else if /*(i == 2) {
                    TextCheckCell actionCell = (TextCheckCell) view;
                    boolean isEnabled = Defaults.getInstance().showTaki();
                    actionCell.setTextAndCheck(LocaleController.getString("MenuTaki", R.string.MenuTaki), isEnabled, false);
                    FontManager.instance().setTypefaceImmediate(view);
                    return view;
                }*/
               /* else if (i == 3) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuChannel", R.string.MenuChannel), R.drawable.menu_channel);
                } */ if (i == 2) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuHistory", R.string.MenuHistory), R.drawable.my_menu_history);
                }
             /*   else if (i == 3) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuTransfare", R.string.MenuTransfare), R.drawable.my_menu_transfare);
                }*/
                else if (i == 3) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuMilShow", R.string.MenuMilShow), R.drawable.my_menu_money);
                } else if (i == 4) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuFAQ", R.string.MenuFAQ), R.drawable.my_menu_faq);
                } else if (i == 5) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuBackup", R.string.MenuBackup), R.drawable.ic_backup);
                } else if (i == 6) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuHelp", R.string.MenuHelp), R.drawable.menu_help);
                } else if (i == 7) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuRef", R.string.MenuRef), R.drawable.my_menu_ref);
                } else if (i == 8) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuShare", R.string.MenuShare), R.drawable.my_menu_share);
                } /*else if (i == 13) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuRate", R.string.MenuRate), R.drawable.my_menu_rate);
                }*/ else if (i == 9) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuRules", R.string.MenuRules), R.drawable.my_menu_rules);
                } else if (i == 10) {
                    DrawerActionCell actionCell = (DrawerActionCell) view;
                    actionCell.setTextAndIcon(LocaleController.getString("MenuExit", R.string.MenuExit), R.drawable.menu_settings);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        FontManager.instance().setTypefaceImmediate(view);

        return view;
    }

    @Override
    public int getItemViewType(int i) {
        if (i == 0) {
            return 0;
        } else if (i == 1) {
            return 1;
        }
        return 3;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public boolean isEmpty() {
        return !UserConfig.isClientActivated();
    }
}
