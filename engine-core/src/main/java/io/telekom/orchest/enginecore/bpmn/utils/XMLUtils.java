package io.telekom.orchest.enginecore.bpmn.utils;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;

/**
 * Utility class for XML processing operations. Provides methods for XML canonicalization,
 * comparison, and minification. Uses Apache XML Security library for canonicalization operations.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XMLUtils {
  private static final Transformer transformer;

  static {
    try {
      transformer = TransformerFactory.newInstance().newTransformer();
      // Configure transformer to output without indentation
      transformer.setOutputProperty(OutputKeys.INDENT, "no");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException(e);
    }
    // Required once per JVM
    Init.init();
  }

  /**
   * Compares two XML strings for equality after canonicalization. Uses XML canonicalization (C14N)
   * to normalize whitespace and formatting before comparison, ensuring that semantically equivalent
   * XML documents are considered equal regardless of formatting differences.
   *
   * @param xml1 The first XML string to compare.
   * @param xml2 The second XML string to compare.
   * @return true if the canonicalized XML strings are equal, false otherwise.
   * @throws NullPointerException if either xml1 or xml2 is null.
   * @throws IllegalStateException if canonicalization fails.
   */
  public static boolean areDefinitionsEqual(String xml1, String xml2) {
    Objects.requireNonNull(xml1, "xml1 must not be null");
    Objects.requireNonNull(xml2, "xml2 must not be null");

    try {
      Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);

      byte[] c1 = canonicalize(canon, xml1);
      byte[] c2 = canonicalize(canon, xml2);

      return MessageDigest.isEqual(c1, c2);

    } catch (Exception e) {
      throw new IllegalStateException("Failed to canonicalize XML", e);
    }
  }

  /**
   * Canonicalizes an XML string using the provided canonicalizer.
   *
   * @param canon The canonicalizer instance to use.
   * @param xml The XML string to canonicalize.
   * @return The canonicalized XML as a byte array.
   * @throws Exception if canonicalization fails.
   */
  private static byte[] canonicalize(Canonicalizer canon, String xml) throws Exception {

    ByteArrayOutputStream out = new ByteArrayOutputStream();

    canon.canonicalize(
        xml.getBytes(StandardCharsets.UTF_8), // input
        out, // output
        false // secureValidation
        );

    return out.toByteArray();
  }

  /**
   * Minifies an XML string by removing unnecessary whitespace, newlines, and indentation. Uses
   * standard Java XML Transformer API to properly handle XML structure, then removes whitespace
   * between tags. Preserves whitespace within text content and attribute values.
   *
   * @param xml the XML string to minify
   * @return the minified XML string
   * @throws NullPointerException if xml is null
   * @throws IllegalStateException if XML transformation fails
   */
  public static String minifyXml(String xml) {
    Objects.requireNonNull(xml, "xml must not be null");

    if (xml.trim().isEmpty()) {
      return xml;
    }

    try {
      // Transform XML to normalize structure
      StringWriter writer = new StringWriter();
      transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(writer));

      String transformed = writer.toString();

      // Remove all whitespace between tags (whitespace between > and <)
      // This regex safely removes whitespace (spaces, tabs, newlines) between closing and opening
      // tags
      // It preserves whitespace within text content and attribute values
      String minified = transformed.replaceAll(">\\s+<", "><");

      // Remove whitespace after XML declaration if present
      minified = minified.replaceAll("(?i)(<\\?xml[^>]*\\?>)\\s+", "$1");

      // Remove any leading/trailing whitespace
      return minified.trim();
    } catch (TransformerException e) {
      throw new IllegalStateException("Failed to minify XML", e);
    }
  }
}
