/*
* This software is governed by the CeCILL-B license under French law and
* abiding by the rules of distribution of free software. You can use,
* modify and/or redistribute the software under the terms of the CeCILL-B
* license as circulated by CEA, CNRS and INRIA at the following URL
* "http://www.cecill.info".
*
* As a counterpart to the access to the source code and rights to copy,
* modify and redistribute granted by the license, users are provided only
* with a limited warranty and the software's author, the holder of the
* economic rights, and the successive licensors have only limited
* liability.
*
* In this respect, the user's attention is drawn to the risks associated
* with loading, using, modifying and/or developing or reproducing the
* software by the user in light of its specific status of free software,
* that may mean that it is complicated to manipulate, and that also
* therefore means that it is reserved for developers and experienced
* professionals having in-depth computer knowledge. Users are therefore
* encouraged to load and test the software's suitability as regards their
* requirements in conditions enabling the security of their systems and/or
* data to be ensured and, more generally, to use and operate it in the
* same conditions as regards security.
*
* The fact that you are presently reading this means that you have had
* knowledge of the CeCILL-B license and that you accept its terms.
*/

package fr.gouv.culture.thesaurus.exception;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;


/**
 * The class <code>BusinessException</code> and its subclasses are
 * thrown to indicate error conditions encountered within the business
 * logic of the application.
 */
public class BusinessException extends Exception
{

	private static final long serialVersionUID = -8635642067404895656L;

    //-------------------------------------------------------------------------
    // Constants definition
    //-------------------------------------------------------------------------

	/**
     * The name of the property resource bundle that contains the message
     * format definitions.
     */
    private final static String MESSAGE_BUNDLE = "thesaurus-errors";

    /**
     * Message identifier for the internal server error.
     */
    private static final String INTERNAL_SERVER_ERROR = "internal.server.error";

    //-------------------------------------------------------------------------
    // Instance members definition
    //-------------------------------------------------------------------------

    /**
     * The formatted detail message for this exception.
     */
    private String formattedMessage;

    /**
     * The message code, i.e. the name of the message format in the
     * resource bundle or <code>null</code> if the message is
     * specified as a string directly containing the message format
     * or text.
     */
    private String messageCode;

    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------

    /**
     * Constructs a new exception with the specified detail message.
     * <p>
     * The detail message can be either the actual message text or
     * the identifier of a resource (defined in the
     * {@link #getMessageBundleName exception type resource bundle})
     * that contains the message text.</p>
     *
     * @param  message   the detail message.
     */
    public BusinessException(String message) {
        this(message, (Object[])null);
    }

    /**
     * Constructs a new exception with the specified error.
     *
     * @param  message   the error message.
     */
    public BusinessException(ErrorMessage message) {
        this(message.getMessageId(), (Object[])null);
    }

    /**
     * Constructs a new exception with the specified detail message
     * and cause.
     * <p>
     * The detail message can be either the actual message text or
     * the identifier of a resource (defined in the
     * {@link #getMessageBundleName exception type resource bundle})
     * that contains the message text.</p>
     * <p>
     * Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated
     * in this throwable's detail message.</p>
     *
     * @param  message   the detail message.
     * @param  cause     the cause. A <code>null</code> value is
     *                   allowed to indicate that the cause is
     *                   nonexistent or unknown.
     */
    public BusinessException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Constructs a new exception with the specified cause but no
     * detail message.
     * <p>
     * The detail message of this exception will be the detail
     * message of the cause.</p>
     *
     * @param  cause   the cause.
     *
     * @throws IllegalArgumentException if <code>cause</code> is
     *                                  <code>null</code>.
     */
    public BusinessException(Throwable cause) {
        this(INTERNAL_SERVER_ERROR, null, cause);
    }

    /**
     * Constructs a new exception with the specified detail message
     * format and the arguments to build the message from the format.
     * <p>
     * The format can be either the actual message format or
     * the identifier of a resource (defined in the
     * {@link #getMessageBundleName exception type resource bundle})
     * that contains the message text.</p>
     *
     * @param  format   the message format, compliant with the
     *                  grammar defined by {@link MessageFormat}.
     * @param  data     the arguments to build the detail message
     *                  from the format.
     */
    public BusinessException(String format, Object[] data) {
        super(format);

        this.formattedMessage = this.formatMessage(format, data);
    }

    /**
     * Constructs a new exception with the specified detail message
     * format and the arguments to build the message from the format.
     * <p>
     * The format can be either the actual message format or
     * the identifier of a resource (defined in the
     * {@link #getMessageBundleName exception type resource bundle})
     * that contains the message text.</p>
     * <p>
     * Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated
     * in this throwable's detail message.</p>
     *
     * @param  format   the message format, compliant with the
     *                  grammar defined by {@link MessageFormat}.
     * @param  data     the arguments to build the detail message
     *                  from the format.
     * @param  cause    the cause. A <code>null</code> value is
     *                  allowed to indicate that the cause is
     *                  nonexistent or unknown.
     */
    public BusinessException(String format, Object[] data, Throwable cause) {
        super(format, cause);

        this.formattedMessage = this.formatMessage(format, data);
    }

    /**
     * Constructs a new exception with the specified error and the
     * arguments to build the message from the format.
     * <p>
     * Note that the detail message associated with
     * <code>cause</code> is <i>not</i> automatically incorporated
     * in this throwable's detail message.</p>
     *
     * @param  format   the message error.
     * @param  data     the arguments to build the detail message
     *                  from the format.
     * @param  cause    the cause. A <code>null</code> value is
     *                  allowed to indicate that the cause is
     *                  nonexistent or unknown.
     */
    public BusinessException(ErrorMessage format, Object[] data, Throwable cause) {
    	this(format.getMessageId(), data, cause);
    }

    //-------------------------------------------------------------------------
    // Overridden superclass methods
    //-------------------------------------------------------------------------

    /**
     * Returns the detail message for this exception, formatting
     * it from the message format and argument if need be.
     *
     * @return the detail message.
     */
    @Override
    public String getMessage() {
        return this.formattedMessage;
    }

    //-------------------------------------------------------------------------
    // Specific implementation
    //-------------------------------------------------------------------------

    /**
     * Returns the message code.
     *
     * @return the message code or <code>null</code> if no
     *         corresponding entry could be found in the
     *         {@link #getMessageBundleName resource bundle}.
     */
    public String getMessageCode() {
        return this.messageCode;
    }

    /**
     * Returns the name of the message bundle to use for formatting
     * error messages for this exception type.  The returned name is
     * relative to the CLASSPATH.
     * <p>
     * This implementation always returns
     * "<code>thesaurus-errors</code>".</p>
     *
     * @return the name message bundle for this exception type or
     *         <code>null</code> if no message formatting shall be
     *         attempted.
     */
    protected String getMessageBundleName() {
        return MESSAGE_BUNDLE;
    }

    /**
     * Formats an error message.
     *
     * @param  key    the message format identifier or the message
     *                format itself or a plian text string.
     * @param  args   the arguments to build the error message from
     *                the format or <code>null</code>.
     *
     * @return a formatted error message.
     */
    protected final String formatMessage(String key, Object[] args) {
        String message = null;

        String format = this.getMessageFormat(key);
        if (format != null) {
            try {
                message = MessageFormat.format(format,
                                        (args != null)? args: new Object[0]);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (message == null) {
            if ((args != null) && (args.length != 0)) {
                StringBuilder buffer = new StringBuilder();

                // No format found for this message identifier
                // or message formatting error encountered.
                // => Just dump the message identifier and each of the
                //    arguments as strings.
                buffer.append(key);

                for (int i=0; i<args.length; i++) {
                    buffer.append(" \"")
                          .append(String.valueOf(args[i]))
                          .append('\"');
                }
                message = buffer.toString();
            }
            else {
                message = key;
            }
        }
        return message;
    }

    /**
     * Returns the message format associated to the specified key,
     * the key itself if no resource is associated to key or
     * <code>null</code> if the
     * {@link #getMessageBundleName resource bundle} for this
     * exception type can not be loaded.
     * <p>
     * This method updates the {@link #getMessageCode message code}
     * if the key is found in the message bundle.</p>
     *
     * @param  key   the name of the message format to retrieve.
     *
     * @return the message format associated to the key or
     *         <code>null</code> if no message format can be
     *         retrieved form the resource bundle.
     */
    private final String getMessageFormat(String key) {
        String format = null;

        if ((key != null) && (this.getMessageBundleName() != null)) {
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(
                                this.getMessageBundleName(), Locale.FRANCE);
                try {
                    format = bundle.getString(key);

                    this.messageCode = key;
                }
                catch (MissingResourceException e) { /* Ignore... */ }
            }
            catch (MissingResourceException e) {
                e.printStackTrace();
                format = null;
            }
        }
        // Else: No message key or message formatting not supported.

        return format;
    }
}
