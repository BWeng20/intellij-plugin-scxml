package com.bw.modeldrive.parser;

/**
 * Are more or less generic exception that something get wrong during parsing.<br>
 * Exceptions of this type can normally not be handled, user has to be informed.
 */
public class ParserException extends Exception
{
	/**
	 * Creates a new ParserException
	 *
	 * @param message The message.
	 * @param cause   The cause, can be null.
	 */
	public ParserException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a new ParserException
	 *
	 * @param message The message.
	 */
	public ParserException(String message)
	{
		this(message, null);
	}

}
