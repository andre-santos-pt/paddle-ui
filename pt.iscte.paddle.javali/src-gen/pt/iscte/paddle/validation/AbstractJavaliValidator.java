/*
 * generated by Xtext 2.19.0
 */
package pt.iscte.paddle.validation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.validation.AbstractDeclarativeValidator;

public abstract class AbstractJavaliValidator extends AbstractDeclarativeValidator {
	
	@Override
	protected List<EPackage> getEPackages() {
		List<EPackage> result = new ArrayList<EPackage>();
		result.add(pt.iscte.paddle.javali.JavaliPackage.eINSTANCE);
		return result;
	}
}
