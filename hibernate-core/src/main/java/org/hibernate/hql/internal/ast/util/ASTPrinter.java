/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.hql.internal.ast.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import org.hibernate.hql.internal.ast.tree.DisplayableNode;
import org.hibernate.internal.util.StringHelper;

import antlr.collections.AST;

/**
 * Utility for generating pretty "ASCII art" representations of syntax trees.
 *
 * This class is threadsafe: reuse instances as needed.
 *
 * @author Joshua Davis
 * @author Steve Ebersole
 */
public class ASTPrinter {

	private final Map<Integer,String> tokenTypeNameCache;

	/**
	 * Constructs a printer. Package protected: use the constants from {TokenPrinters}
	 * @see TokenPrinters
	 *
	 * @param tokenTypeConstants The token types to use during printing; typically the {vocabulary}TokenTypes.java
	 * interface generated by ANTLR.
	 */
	ASTPrinter(Class tokenTypeConstants) {
		this.tokenTypeNameCache = ASTUtil.generateTokenNameCache( tokenTypeConstants );
	}

	/**
	 * Renders the AST into 'ASCII art' form and returns that string representation.
	 *
	 * @param ast The AST to display.
	 * @param header The header for the display.
	 *
	 * @return The AST in 'ASCII art' form, as a string.
	 */
	public String showAsString(AST ast, String header) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream( baos );
		ps.println( header );
		showAst( ast, ps );
		ps.flush();
		return new String( baos.toByteArray() );
	}

	/**
	 * Prints the AST in 'ASCII art' form to the specified print stream.
	 *
	 * @param ast The AST to print.
	 * @param out The print stream to which the AST should be printed.
	 */
	public void showAst(AST ast, PrintStream out) {
		showAst( ast, new PrintWriter( out ) );
	}

	/**
	 * Prints the AST in 'ASCII art' tree form to the specified print writer.
	 *
	 * @param ast The AST to print.
	 * @param pw The print writer to which the AST should be written.
	 */
	public void showAst(AST ast, PrintWriter pw) {
		ArrayList<AST> parents = new ArrayList<AST>();
		showAst( parents, pw, ast );
		pw.flush();
	}

	/**
	 * Returns the token type name for the given token type.
	 *
	 * @param type The token type.
	 *
	 * @return String - The token type name from the token type constant class,
	 *         or just the integer as a string if none exists.
	 */
	public String getTokenTypeName(int type) {
		final Integer typeInteger = type;
		String value = tokenTypeNameCache.get( typeInteger );
		if ( value == null ) {
			value = typeInteger.toString();
		}
		return value;
	}

	private void showAst(ArrayList<AST> parents, PrintWriter pw, AST ast) {
		if ( ast == null ) {
			pw.println( "AST is null!" );
			return;
		}

		for ( AST parent : parents ) {
			if ( parent.getNextSibling() == null ) {

				pw.print( "   " );
			}
			else {
				pw.print( " | " );
			}
		}

		if ( ast.getNextSibling() == null ) {
			pw.print( " \\-" );
		}
		else {
			pw.print( " +-" );
		}

		showNode( pw, ast );

		ArrayList<AST> newParents = new ArrayList<AST>( parents );
		newParents.add( ast );
		for ( AST child = ast.getFirstChild(); child != null; child = child.getNextSibling() ) {
			showAst( newParents, pw, child );
		}
		newParents.clear();
	}

	private void showNode(PrintWriter pw, AST ast) {
		String s = nodeToString( ast );
		pw.println( s );
	}

	public String nodeToString(AST ast) {
		if ( ast == null ) {
			return "{node:null}";
		}
		StringBuilder buf = new StringBuilder();
		buf.append( "[" ).append( getTokenTypeName( ast.getType() ) ).append( "] " );
		buf.append( StringHelper.unqualify( ast.getClass().getName() ) ).append( ": " );

		buf.append( "'" );
		String text = ast.getText();
		if ( text == null ) {
			text = "{text:null}";
		}
		appendEscapedMultibyteChars( text, buf );
		buf.append( "'" );
		if ( ast instanceof DisplayableNode ) {
			DisplayableNode displayableNode = (DisplayableNode) ast;
			// Add a space before the display text.
			buf.append( " " ).append( displayableNode.getDisplayText() );
		}
		return buf.toString();
	}

	public static void appendEscapedMultibyteChars(String text, StringBuilder buf) {
		char[] chars = text.toCharArray();
		for ( char aChar : chars ) {
			if ( aChar > 256 ) {
				buf.append( "\\u" );
				buf.append( Integer.toHexString( aChar ) );
			}
			else {
				buf.append( aChar );
			}
		}
	}

	public static String escapeMultibyteChars(String text) {
		StringBuilder buf = new StringBuilder();
		appendEscapedMultibyteChars( text, buf );
		return buf.toString();
	}
}
