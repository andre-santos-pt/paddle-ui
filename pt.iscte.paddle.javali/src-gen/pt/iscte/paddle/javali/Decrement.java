/**
 * generated by Xtext 2.19.0
 */
package pt.iscte.paddle.javali;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Decrement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link pt.iscte.paddle.javali.Decrement#getId <em>Id</em>}</li>
 * </ul>
 *
 * @see pt.iscte.paddle.javali.JavaliPackage#getDecrement()
 * @model
 * @generated
 */
public interface Decrement extends Statement
{
  /**
   * Returns the value of the '<em><b>Id</b></em>' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @return the value of the '<em>Id</em>' containment reference.
   * @see #setId(Identifier)
   * @see pt.iscte.paddle.javali.JavaliPackage#getDecrement_Id()
   * @model containment="true"
   * @generated
   */
  Identifier getId();

  /**
   * Sets the value of the '{@link pt.iscte.paddle.javali.Decrement#getId <em>Id</em>}' containment reference.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Id</em>' containment reference.
   * @see #getId()
   * @generated
   */
  void setId(Identifier value);

} // Decrement
