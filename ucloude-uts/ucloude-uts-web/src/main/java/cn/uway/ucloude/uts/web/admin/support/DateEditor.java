package cn.uway.ucloude.uts.web.admin.support;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.uway.ucloude.utils.DateUtil;
import cn.uway.ucloude.utils.StringUtil;

/**
 * Date转换器
 * 
 */
public class DateEditor extends PropertyEditorSupport {
//  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final DateFormat TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  private DateFormat dateFormat;
  private boolean allowEmpty = true;

  public DateEditor() {
  }

  public DateEditor(DateFormat dateFormat) {
      this.dateFormat = dateFormat;
  }

  public DateEditor(DateFormat dateFormat, boolean allowEmpty) {
      this.dateFormat = dateFormat;
      this.allowEmpty = allowEmpty;
  }

  /**
   * Parse the Date from the given text, using the specified DateFormat.
   */
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
      if (this.allowEmpty && !StringUtil.hasText(text)) {
          // Treat empty String as null value.
          setValue(null);
      } else {
          try {
              if (this.dateFormat != null)
                  setValue(this.dateFormat.parse(text));
              else {
                  setValue(DateUtil.parse(text));
              }
          } catch (ParseException ex) {
              throw new IllegalArgumentException("Could not parse date: " + ex.getMessage(), ex);
          }
      }
  }

  /**
   * Format the Date as String, using the specified DateFormat.
   */
  @Override
  public String getAsText() {
      Date value = (Date) getValue();
      DateFormat dateFormat = this.dateFormat;
      if (dateFormat == null)
          dateFormat = TIME_FORMAT;
      return (value != null ? dateFormat.format(value) : "");
  }
}
