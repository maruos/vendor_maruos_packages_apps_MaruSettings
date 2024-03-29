package com.maru.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.maru.settings.R;

import java.util.ArrayList;

public class SwitchBar extends LinearLayout
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    public interface OnSwitchChangeListener {
        /**
         * Called when the checked state of the Switch has changed.
         *
         * @param switchView The Switch view whose state has changed.
         * @param isChecked The new checked state of switchView.
         */
        void onSwitchChanged(Switch switchView, boolean isChecked);
    }

    private final TextAppearanceSpan mSummarySpan;

    private ToggleSwitch mSwitch;
    private TextView mTextView;
    private String mLabel;
    private String mSummary;

    private final ArrayList<OnSwitchChangeListener> mSwitchChangeListeners = new ArrayList<>();

    private static final int[] XML_ATTRIBUTES = {
        R.attr.switchBarMarginStart, R.attr.switchBarMarginEnd, R.attr.switchBarBackgroundColor
    };

    public SwitchBar(Context context) {
        this(context, null);
    }

    public SwitchBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        LayoutInflater.from(context).inflate(R.layout.switch_bar, this);

        final TypedArray a = context.obtainStyledAttributes(attrs, XML_ATTRIBUTES);
        int switchBarMarginStart = (int) a.getDimension(0, 0);
        int switchBarMarginEnd = (int) a.getDimension(1, 0);
        int switchBarBackgroundColor = (int) a.getColor(2, 0);
        a.recycle();

        mTextView = findViewById(R.id.switch_text);
        mTextView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        mLabel = getResources().getString(R.string.switch_off_text);
        mSummarySpan = new TextAppearanceSpan(mContext, R.style.TextAppearance_Small_SwitchBar);
        updateText();
        ViewGroup.MarginLayoutParams lp = (MarginLayoutParams) mTextView.getLayoutParams();
        lp.setMarginStart(switchBarMarginStart);

        mSwitch = findViewById(R.id.switch_widget);
        // Prevent onSaveInstanceState() to be called as we are managing the state of the Switch
        // on our own
        mSwitch.setSaveEnabled(false);
        mSwitch.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        lp = (MarginLayoutParams) mSwitch.getLayoutParams();
        lp.setMarginEnd(switchBarMarginEnd);
        setBackgroundColor(switchBarBackgroundColor);

        addOnSwitchChangeListener((switchView, isChecked) -> setTextViewLabel(isChecked));

        setOnClickListener(this);

        // Default is hide
        setVisibility(View.GONE);
    }

    public void setTextViewLabel(boolean isChecked) {
        mLabel =
                getResources()
                        .getString(isChecked ? R.string.switch_on_text : R.string.switch_off_text);
        updateText();
    }

    public void setSummary(String summary) {
        mSummary = summary;
        updateText();
    }

    private void updateText() {
        if (TextUtils.isEmpty(mSummary)) {
            mTextView.setText(mLabel);
            return;
        }
        final SpannableStringBuilder ssb = new SpannableStringBuilder(mLabel).append('\n');
        final int start = ssb.length();
        ssb.append(mSummary);
        ssb.setSpan(mSummarySpan, start, ssb.length(), 0);
        mTextView.setText(ssb);
    }

    public void setChecked(boolean checked) {
        setTextViewLabel(checked);
        mSwitch.setChecked(checked);
    }

    public void setCheckedInternal(boolean checked) {
        setTextViewLabel(checked);
        mSwitch.setCheckedInternal(checked);
    }

    public boolean isChecked() {
        return mSwitch.isChecked();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextView.setEnabled(enabled);
        mSwitch.setEnabled(enabled);
    }

    public final ToggleSwitch getSwitch() {
        return mSwitch;
    }

    public void show() {
        if (!isShowing()) {
            setVisibility(View.VISIBLE);
            mSwitch.setOnCheckedChangeListener(this);
        }
    }

    public void hide() {
        if (isShowing()) {
            setVisibility(View.GONE);
            mSwitch.setOnCheckedChangeListener(null);
        }
    }

    public boolean isShowing() {
        return (getVisibility() == View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        final boolean isChecked = !mSwitch.isChecked();
        setChecked(isChecked);
    }

    public void propagateChecked(boolean isChecked) {
        for (OnSwitchChangeListener switchChangeListener : mSwitchChangeListeners) {
            switchChangeListener.onSwitchChanged(mSwitch, isChecked);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        propagateChecked(isChecked);
    }

    public void addOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot add twice the same OnSwitchChangeListener");
        }
        mSwitchChangeListeners.add(listener);
    }

    public void removeOnSwitchChangeListener(OnSwitchChangeListener listener) {
        if (!mSwitchChangeListeners.contains(listener)) {
            throw new IllegalStateException("Cannot remove OnSwitchChangeListener");
        }
        mSwitchChangeListeners.remove(listener);
    }

    static class SavedState extends BaseSavedState {
        boolean checked;
        boolean visible;

        SavedState(Parcelable superState) {
            super(superState);
        }

        /** Constructor called from {@link #CREATOR} */
        private SavedState(Parcel in) {
            super(in);
            checked = (Boolean) in.readValue(null);
            visible = (Boolean) in.readValue(null);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeValue(checked);
            out.writeValue(visible);
        }

        @Override
        public String toString() {
            return "SwitchBar.SavedState{"
                    + Integer.toHexString(System.identityHashCode(this))
                    + " checked="
                    + checked
                    + " visible="
                    + visible
                    + "}";
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.checked = mSwitch.isChecked();
        ss.visible = isShowing();
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;

        super.onRestoreInstanceState(ss.getSuperState());

        mSwitch.setCheckedInternal(ss.checked);
        setTextViewLabel(ss.checked);
        setVisibility(ss.visible ? View.VISIBLE : View.GONE);
        mSwitch.setOnCheckedChangeListener(ss.visible ? this : null);

        requestLayout();
    }

    @Override
    public CharSequence getAccessibilityClassName() {
        return Switch.class.getName();
    }

    @Override
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        // Since the children are marked as not important for accessibility, re-dispatch all
        // of their events as if they came from this view
        event.setSource(this);
        return true;
    }

    /** @hide */
    @Override
    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        info.setText(mTextView.getText());
        info.setCheckable(true);
        info.setChecked(mSwitch.isChecked());
    }

    /** @hide */
    @Override
    public void onInitializeAccessibilityEventInternal(AccessibilityEvent event) {
        super.onInitializeAccessibilityEventInternal(event);
        // Don't say "on on" or "off off" - rather, speak the state only once. We need to specify
        // this explicitly as each of our children (the textview and the checkbox) contribute to
        // the state once, giving us duplicate text by default.
        event.setContentDescription(mTextView.getText());
        event.setChecked(mSwitch.isChecked());
    }
}
