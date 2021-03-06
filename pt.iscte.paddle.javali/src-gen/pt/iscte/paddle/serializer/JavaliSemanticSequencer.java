/*
 * generated by Xtext 2.19.0
 */
package pt.iscte.paddle.serializer;

import com.google.inject.Inject;
import java.util.Set;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.xtext.Action;
import org.eclipse.xtext.Parameter;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.serializer.ISerializationContext;
import org.eclipse.xtext.serializer.acceptor.SequenceFeeder;
import org.eclipse.xtext.serializer.sequencer.AbstractDelegatingSemanticSequencer;
import org.eclipse.xtext.serializer.sequencer.ITransientValueService.ValueTransient;
import pt.iscte.paddle.javali.Addition;
import pt.iscte.paddle.javali.And;
import pt.iscte.paddle.javali.Block;
import pt.iscte.paddle.javali.Break;
import pt.iscte.paddle.javali.Constant;
import pt.iscte.paddle.javali.Continue;
import pt.iscte.paddle.javali.Decrement;
import pt.iscte.paddle.javali.DoWhile;
import pt.iscte.paddle.javali.Equality;
import pt.iscte.paddle.javali.For;
import pt.iscte.paddle.javali.Identifier;
import pt.iscte.paddle.javali.IfElse;
import pt.iscte.paddle.javali.Increment;
import pt.iscte.paddle.javali.JavaliPackage;
import pt.iscte.paddle.javali.Literal;
import pt.iscte.paddle.javali.Multiplication;
import pt.iscte.paddle.javali.NewArray;
import pt.iscte.paddle.javali.NewObject;
import pt.iscte.paddle.javali.Null;
import pt.iscte.paddle.javali.Or;
import pt.iscte.paddle.javali.ProcCall;
import pt.iscte.paddle.javali.Procedure;
import pt.iscte.paddle.javali.Record;
import pt.iscte.paddle.javali.Relation;
import pt.iscte.paddle.javali.Return;
import pt.iscte.paddle.javali.Type;
import pt.iscte.paddle.javali.VarAssign;
import pt.iscte.paddle.javali.VarDeclaration;
import pt.iscte.paddle.javali.VarExpression;
import pt.iscte.paddle.javali.While;
import pt.iscte.paddle.javali.Xor;
import pt.iscte.paddle.services.JavaliGrammarAccess;

@SuppressWarnings("all")
public class JavaliSemanticSequencer extends AbstractDelegatingSemanticSequencer {

	@Inject
	private JavaliGrammarAccess grammarAccess;
	
	@Override
	public void sequence(ISerializationContext context, EObject semanticObject) {
		EPackage epackage = semanticObject.eClass().getEPackage();
		ParserRule rule = context.getParserRule();
		Action action = context.getAssignedAction();
		Set<Parameter> parameters = context.getEnabledBooleanParameters();
		if (epackage == JavaliPackage.eINSTANCE)
			switch (semanticObject.eClass().getClassifierID()) {
			case JavaliPackage.ADDITION:
				sequence_Addition(context, (Addition) semanticObject); 
				return; 
			case JavaliPackage.AND:
				sequence_And(context, (And) semanticObject); 
				return; 
			case JavaliPackage.BLOCK:
				sequence_Block(context, (Block) semanticObject); 
				return; 
			case JavaliPackage.BREAK:
				sequence_Break(context, (Break) semanticObject); 
				return; 
			case JavaliPackage.CONSTANT:
				sequence_Constant(context, (Constant) semanticObject); 
				return; 
			case JavaliPackage.CONTINUE:
				sequence_Continue(context, (Continue) semanticObject); 
				return; 
			case JavaliPackage.DECREMENT:
				sequence_Decrement(context, (Decrement) semanticObject); 
				return; 
			case JavaliPackage.DO_WHILE:
				sequence_DoWhile(context, (DoWhile) semanticObject); 
				return; 
			case JavaliPackage.EQUALITY:
				sequence_Equality(context, (Equality) semanticObject); 
				return; 
			case JavaliPackage.FOR:
				sequence_For(context, (For) semanticObject); 
				return; 
			case JavaliPackage.IDENTIFIER:
				sequence_Identifier(context, (Identifier) semanticObject); 
				return; 
			case JavaliPackage.IF_ELSE:
				sequence_IfElse(context, (IfElse) semanticObject); 
				return; 
			case JavaliPackage.INCREMENT:
				sequence_Increment(context, (Increment) semanticObject); 
				return; 
			case JavaliPackage.LITERAL:
				sequence_Literal(context, (Literal) semanticObject); 
				return; 
			case JavaliPackage.MODULE:
				sequence_Module(context, (pt.iscte.paddle.javali.Module) semanticObject); 
				return; 
			case JavaliPackage.MULTIPLICATION:
				sequence_Multiplication(context, (Multiplication) semanticObject); 
				return; 
			case JavaliPackage.NEW_ARRAY:
				sequence_NewArray(context, (NewArray) semanticObject); 
				return; 
			case JavaliPackage.NEW_OBJECT:
				sequence_NewObject(context, (NewObject) semanticObject); 
				return; 
			case JavaliPackage.NULL:
				sequence_Null(context, (Null) semanticObject); 
				return; 
			case JavaliPackage.OR:
				sequence_Or(context, (Or) semanticObject); 
				return; 
			case JavaliPackage.PROC_CALL:
				sequence_ProcCall(context, (ProcCall) semanticObject); 
				return; 
			case JavaliPackage.PROCEDURE:
				sequence_Procedure(context, (Procedure) semanticObject); 
				return; 
			case JavaliPackage.RECORD:
				sequence_Record(context, (Record) semanticObject); 
				return; 
			case JavaliPackage.RELATION:
				sequence_Relation(context, (Relation) semanticObject); 
				return; 
			case JavaliPackage.RETURN:
				sequence_Return(context, (Return) semanticObject); 
				return; 
			case JavaliPackage.TYPE:
				sequence_Type(context, (Type) semanticObject); 
				return; 
			case JavaliPackage.VAR_ASSIGN:
				sequence_VarAssign(context, (VarAssign) semanticObject); 
				return; 
			case JavaliPackage.VAR_DECLARATION:
				if (rule == grammarAccess.getStatementRule()
						|| rule == grammarAccess.getVarDeclarationAssignRule()
						|| rule == grammarAccess.getForStatementInitRule()) {
					sequence_VarDeclarationAssign(context, (VarDeclaration) semanticObject); 
					return; 
				}
				else if (rule == grammarAccess.getVarDeclarationRule()) {
					sequence_VarDeclaration(context, (VarDeclaration) semanticObject); 
					return; 
				}
				else break;
			case JavaliPackage.VAR_EXPRESSION:
				sequence_VarExpression(context, (VarExpression) semanticObject); 
				return; 
			case JavaliPackage.WHILE:
				sequence_While(context, (While) semanticObject); 
				return; 
			case JavaliPackage.XOR:
				sequence_Xor(context, (Xor) semanticObject); 
				return; 
			}
		if (errorAcceptor != null)
			errorAcceptor.accept(diagnosticProvider.createInvalidContextOrTypeDiagnostic(semanticObject, context));
	}
	
	/**
	 * Contexts:
	 *     Expression returns Addition
	 *     Or returns Addition
	 *     Or.Or_1_0 returns Addition
	 *     Xor returns Addition
	 *     Xor.Xor_1_0 returns Addition
	 *     And returns Addition
	 *     And.And_1_0 returns Addition
	 *     Equality returns Addition
	 *     Equality.Equality_1_0 returns Addition
	 *     Relation returns Addition
	 *     Relation.Relation_1_0 returns Addition
	 *     Addition returns Addition
	 *     Addition.Addition_1_0 returns Addition
	 *     Multiplication returns Addition
	 *     Multiplication.Multiplication_1_0 returns Addition
	 *     Primary returns Addition
	 *
	 * Constraint:
	 *     (left=Addition_Addition_1_0 (operator='+' | operator='-') right=Multiplication)
	 */
	protected void sequence_Addition(ISerializationContext context, Addition semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns And
	 *     Or returns And
	 *     Or.Or_1_0 returns And
	 *     Xor returns And
	 *     Xor.Xor_1_0 returns And
	 *     And returns And
	 *     And.And_1_0 returns And
	 *     Equality returns And
	 *     Equality.Equality_1_0 returns And
	 *     Relation returns And
	 *     Relation.Relation_1_0 returns And
	 *     Addition returns And
	 *     Addition.Addition_1_0 returns And
	 *     Multiplication returns And
	 *     Multiplication.Multiplication_1_0 returns And
	 *     Primary returns And
	 *
	 * Constraint:
	 *     (left=And_And_1_0 right=Equality)
	 */
	protected void sequence_And(ISerializationContext context, And semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.AND__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.AND__LEFT));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.AND__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.AND__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getAndAccess().getAndLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getAndAccess().getRightEqualityParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Block returns Block
	 *
	 * Constraint:
	 *     statements+=Statement*
	 */
	protected void sequence_Block(ISerializationContext context, Block semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns Break
	 *     Break returns Break
	 *
	 * Constraint:
	 *     {Break}
	 */
	protected void sequence_Break(ISerializationContext context, Break semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Constant returns Constant
	 *
	 * Constraint:
	 *     (static?='static'? type=Type id=Identifier value=Literal)
	 */
	protected void sequence_Constant(ISerializationContext context, Constant semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns Continue
	 *     Continue returns Continue
	 *
	 * Constraint:
	 *     {Continue}
	 */
	protected void sequence_Continue(ISerializationContext context, Continue semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns Decrement
	 *     ForStatement returns Decrement
	 *     Decrement returns Decrement
	 *
	 * Constraint:
	 *     id=Identifier
	 */
	protected void sequence_Decrement(ISerializationContext context, Decrement semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.DECREMENT__ID) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.DECREMENT__ID));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getDecrementAccess().getIdIdentifierParserRuleCall_0_0(), semanticObject.getId());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns DoWhile
	 *     DoWhile returns DoWhile
	 *
	 * Constraint:
	 *     (block=Block guard=Expression)
	 */
	protected void sequence_DoWhile(ISerializationContext context, DoWhile semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.DO_WHILE__BLOCK) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.DO_WHILE__BLOCK));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.DO_WHILE__GUARD) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.DO_WHILE__GUARD));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getDoWhileAccess().getBlockBlockParserRuleCall_1_0(), semanticObject.getBlock());
		feeder.accept(grammarAccess.getDoWhileAccess().getGuardExpressionParserRuleCall_4_0(), semanticObject.getGuard());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Equality
	 *     Or returns Equality
	 *     Or.Or_1_0 returns Equality
	 *     Xor returns Equality
	 *     Xor.Xor_1_0 returns Equality
	 *     And returns Equality
	 *     And.And_1_0 returns Equality
	 *     Equality returns Equality
	 *     Equality.Equality_1_0 returns Equality
	 *     Relation returns Equality
	 *     Relation.Relation_1_0 returns Equality
	 *     Addition returns Equality
	 *     Addition.Addition_1_0 returns Equality
	 *     Multiplication returns Equality
	 *     Multiplication.Multiplication_1_0 returns Equality
	 *     Primary returns Equality
	 *
	 * Constraint:
	 *     (left=Equality_Equality_1_0 (operator='==' | operator='!=') right=Relation)
	 */
	protected void sequence_Equality(ISerializationContext context, Equality semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns For
	 *     For returns For
	 *
	 * Constraint:
	 *     (
	 *         (initStatements+=ForStatementInit initStatements+=ForStatementInit*)? 
	 *         guard=Expression 
	 *         (progressStatements+=ForStatement progressStatements+=ForStatement*)? 
	 *         block=Block
	 *     )
	 */
	protected void sequence_For(ISerializationContext context, For semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Identifier returns Identifier
	 *
	 * Constraint:
	 *     id=ID
	 */
	protected void sequence_Identifier(ISerializationContext context, Identifier semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.IDENTIFIER__ID) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.IDENTIFIER__ID));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getIdentifierAccess().getIdIDTerminalRuleCall_0(), semanticObject.getId());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns IfElse
	 *     IfElse returns IfElse
	 *
	 * Constraint:
	 *     (guard=Expression selectionBlock=Block alternativeBlock=Block?)
	 */
	protected void sequence_IfElse(ISerializationContext context, IfElse semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns Increment
	 *     ForStatement returns Increment
	 *     Increment returns Increment
	 *
	 * Constraint:
	 *     id=Identifier
	 */
	protected void sequence_Increment(ISerializationContext context, Increment semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.INCREMENT__ID) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.INCREMENT__ID));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getIncrementAccess().getIdIdentifierParserRuleCall_0_0(), semanticObject.getId());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Literal
	 *     Or returns Literal
	 *     Or.Or_1_0 returns Literal
	 *     Xor returns Literal
	 *     Xor.Xor_1_0 returns Literal
	 *     And returns Literal
	 *     And.And_1_0 returns Literal
	 *     Equality returns Literal
	 *     Equality.Equality_1_0 returns Literal
	 *     Relation returns Literal
	 *     Relation.Relation_1_0 returns Literal
	 *     Addition returns Literal
	 *     Addition.Addition_1_0 returns Literal
	 *     Multiplication returns Literal
	 *     Multiplication.Multiplication_1_0 returns Literal
	 *     Primary returns Literal
	 *     Literal returns Literal
	 *
	 * Constraint:
	 *     value=PRIMITIVE_VALUE
	 */
	protected void sequence_Literal(ISerializationContext context, Literal semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.LITERAL__VALUE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.LITERAL__VALUE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getLiteralAccess().getValuePRIMITIVE_VALUETerminalRuleCall_0(), semanticObject.getValue());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Module returns Module
	 *
	 * Constraint:
	 *     (constants+=Constant | records+=Record | procedures+=Procedure)+
	 */
	protected void sequence_Module(ISerializationContext context, pt.iscte.paddle.javali.Module semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Multiplication
	 *     Or returns Multiplication
	 *     Or.Or_1_0 returns Multiplication
	 *     Xor returns Multiplication
	 *     Xor.Xor_1_0 returns Multiplication
	 *     And returns Multiplication
	 *     And.And_1_0 returns Multiplication
	 *     Equality returns Multiplication
	 *     Equality.Equality_1_0 returns Multiplication
	 *     Relation returns Multiplication
	 *     Relation.Relation_1_0 returns Multiplication
	 *     Addition returns Multiplication
	 *     Addition.Addition_1_0 returns Multiplication
	 *     Multiplication returns Multiplication
	 *     Multiplication.Multiplication_1_0 returns Multiplication
	 *     Primary returns Multiplication
	 *
	 * Constraint:
	 *     (left=Multiplication_Multiplication_1_0 (operator='*' | operator='/' | operator='%') right=Primary)
	 */
	protected void sequence_Multiplication(ISerializationContext context, Multiplication semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns NewArray
	 *     Or returns NewArray
	 *     Or.Or_1_0 returns NewArray
	 *     Xor returns NewArray
	 *     Xor.Xor_1_0 returns NewArray
	 *     And returns NewArray
	 *     And.And_1_0 returns NewArray
	 *     Equality returns NewArray
	 *     Equality.Equality_1_0 returns NewArray
	 *     Relation returns NewArray
	 *     Relation.Relation_1_0 returns NewArray
	 *     Addition returns NewArray
	 *     Addition.Addition_1_0 returns NewArray
	 *     Multiplication returns NewArray
	 *     Multiplication.Multiplication_1_0 returns NewArray
	 *     Primary returns NewArray
	 *     NewArray returns NewArray
	 *
	 * Constraint:
	 *     (type=Identifier arrayDims+=Expression+)
	 */
	protected void sequence_NewArray(ISerializationContext context, NewArray semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns NewObject
	 *     Or returns NewObject
	 *     Or.Or_1_0 returns NewObject
	 *     Xor returns NewObject
	 *     Xor.Xor_1_0 returns NewObject
	 *     And returns NewObject
	 *     And.And_1_0 returns NewObject
	 *     Equality returns NewObject
	 *     Equality.Equality_1_0 returns NewObject
	 *     Relation returns NewObject
	 *     Relation.Relation_1_0 returns NewObject
	 *     Addition returns NewObject
	 *     Addition.Addition_1_0 returns NewObject
	 *     Multiplication returns NewObject
	 *     Multiplication.Multiplication_1_0 returns NewObject
	 *     Primary returns NewObject
	 *     NewObject returns NewObject
	 *
	 * Constraint:
	 *     type=Identifier
	 */
	protected void sequence_NewObject(ISerializationContext context, NewObject semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.NEW_OBJECT__TYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.NEW_OBJECT__TYPE));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getNewObjectAccess().getTypeIdentifierParserRuleCall_1_0(), semanticObject.getType());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Null
	 *     Or returns Null
	 *     Or.Or_1_0 returns Null
	 *     Xor returns Null
	 *     Xor.Xor_1_0 returns Null
	 *     And returns Null
	 *     And.And_1_0 returns Null
	 *     Equality returns Null
	 *     Equality.Equality_1_0 returns Null
	 *     Relation returns Null
	 *     Relation.Relation_1_0 returns Null
	 *     Addition returns Null
	 *     Addition.Addition_1_0 returns Null
	 *     Multiplication returns Null
	 *     Multiplication.Multiplication_1_0 returns Null
	 *     Primary returns Null
	 *     Null returns Null
	 *
	 * Constraint:
	 *     {Null}
	 */
	protected void sequence_Null(ISerializationContext context, Null semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Or
	 *     Or returns Or
	 *     Or.Or_1_0 returns Or
	 *     Xor returns Or
	 *     Xor.Xor_1_0 returns Or
	 *     And returns Or
	 *     And.And_1_0 returns Or
	 *     Equality returns Or
	 *     Equality.Equality_1_0 returns Or
	 *     Relation returns Or
	 *     Relation.Relation_1_0 returns Or
	 *     Addition returns Or
	 *     Addition.Addition_1_0 returns Or
	 *     Multiplication returns Or
	 *     Multiplication.Multiplication_1_0 returns Or
	 *     Primary returns Or
	 *
	 * Constraint:
	 *     (left=Or_Or_1_0 right=Xor)
	 */
	protected void sequence_Or(ISerializationContext context, Or semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.OR__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.OR__LEFT));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.OR__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.OR__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getOrAccess().getOrLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getOrAccess().getRightXorParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns ProcCall
	 *     Expression returns ProcCall
	 *     Or returns ProcCall
	 *     Or.Or_1_0 returns ProcCall
	 *     Xor returns ProcCall
	 *     Xor.Xor_1_0 returns ProcCall
	 *     And returns ProcCall
	 *     And.And_1_0 returns ProcCall
	 *     Equality returns ProcCall
	 *     Equality.Equality_1_0 returns ProcCall
	 *     Relation returns ProcCall
	 *     Relation.Relation_1_0 returns ProcCall
	 *     Addition returns ProcCall
	 *     Addition.Addition_1_0 returns ProcCall
	 *     Multiplication returns ProcCall
	 *     Multiplication.Multiplication_1_0 returns ProcCall
	 *     Primary returns ProcCall
	 *     ProcCall returns ProcCall
	 *
	 * Constraint:
	 *     (id=Identifier (args+=Expression args+=Expression*)?)
	 */
	protected void sequence_ProcCall(ISerializationContext context, ProcCall semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Procedure returns Procedure
	 *
	 * Constraint:
	 *     (
	 *         comment=ML_COMMENT_DOC? 
	 *         static?='static'? 
	 *         (retType=Type | void?='void') 
	 *         id=Identifier 
	 *         (params+=VarDeclaration params+=VarDeclaration*)? 
	 *         body=Block
	 *     )
	 */
	protected void sequence_Procedure(ISerializationContext context, Procedure semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Record returns Record
	 *
	 * Constraint:
	 *     (id=Identifier fields+=VarDeclaration*)
	 */
	protected void sequence_Record(ISerializationContext context, Record semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Relation
	 *     Or returns Relation
	 *     Or.Or_1_0 returns Relation
	 *     Xor returns Relation
	 *     Xor.Xor_1_0 returns Relation
	 *     And returns Relation
	 *     And.And_1_0 returns Relation
	 *     Equality returns Relation
	 *     Equality.Equality_1_0 returns Relation
	 *     Relation returns Relation
	 *     Relation.Relation_1_0 returns Relation
	 *     Addition returns Relation
	 *     Addition.Addition_1_0 returns Relation
	 *     Multiplication returns Relation
	 *     Multiplication.Multiplication_1_0 returns Relation
	 *     Primary returns Relation
	 *
	 * Constraint:
	 *     (left=Relation_Relation_1_0 (operator='<' | operator='<=' | operator='>' | operator='>=') right=Addition)
	 */
	protected void sequence_Relation(ISerializationContext context, Relation semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns Return
	 *     Return returns Return
	 *
	 * Constraint:
	 *     exp=Expression?
	 */
	protected void sequence_Return(ISerializationContext context, Return semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Type returns Type
	 *
	 * Constraint:
	 *     (id=Identifier arrayDims+='['*)
	 */
	protected void sequence_Type(ISerializationContext context, Type semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns VarAssign
	 *     VarAssign returns VarAssign
	 *     ForStatementInit returns VarAssign
	 *     ForStatement returns VarAssign
	 *
	 * Constraint:
	 *     (var=VarExpression exp=Expression)
	 */
	protected void sequence_VarAssign(ISerializationContext context, VarAssign semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.VAR_ASSIGN__VAR) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.VAR_ASSIGN__VAR));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.VAR_ASSIGN__EXP) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.VAR_ASSIGN__EXP));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getVarAssignAccess().getVarVarExpressionParserRuleCall_0_0(), semanticObject.getVar());
		feeder.accept(grammarAccess.getVarAssignAccess().getExpExpressionParserRuleCall_2_0(), semanticObject.getExp());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns VarDeclaration
	 *     VarDeclarationAssign returns VarDeclaration
	 *     ForStatementInit returns VarDeclaration
	 *
	 * Constraint:
	 *     (type=Type id=Identifier init=Expression?)
	 */
	protected void sequence_VarDeclarationAssign(ISerializationContext context, VarDeclaration semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     VarDeclaration returns VarDeclaration
	 *
	 * Constraint:
	 *     (type=Type id=Identifier)
	 */
	protected void sequence_VarDeclaration(ISerializationContext context, VarDeclaration semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.VAR_DECLARATION__TYPE) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.VAR_DECLARATION__TYPE));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.VAR_DECLARATION__ID) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.VAR_DECLARATION__ID));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getVarDeclarationAccess().getTypeTypeParserRuleCall_0_0(), semanticObject.getType());
		feeder.accept(grammarAccess.getVarDeclarationAccess().getIdIdentifierParserRuleCall_1_0(), semanticObject.getId());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns VarExpression
	 *     Or returns VarExpression
	 *     Or.Or_1_0 returns VarExpression
	 *     Xor returns VarExpression
	 *     Xor.Xor_1_0 returns VarExpression
	 *     And returns VarExpression
	 *     And.And_1_0 returns VarExpression
	 *     Equality returns VarExpression
	 *     Equality.Equality_1_0 returns VarExpression
	 *     Relation returns VarExpression
	 *     Relation.Relation_1_0 returns VarExpression
	 *     Addition returns VarExpression
	 *     Addition.Addition_1_0 returns VarExpression
	 *     Multiplication returns VarExpression
	 *     Multiplication.Multiplication_1_0 returns VarExpression
	 *     Primary returns VarExpression
	 *     VarExpression returns VarExpression
	 *
	 * Constraint:
	 *     (parts+=Identifier arrayIndexes+=Expression* (parts+=Identifier arrayIndexes+=Expression*)*)
	 */
	protected void sequence_VarExpression(ISerializationContext context, VarExpression semanticObject) {
		genericSequencer.createSequence(context, semanticObject);
	}
	
	
	/**
	 * Contexts:
	 *     Statement returns While
	 *     While returns While
	 *
	 * Constraint:
	 *     (guard=Expression block=Block)
	 */
	protected void sequence_While(ISerializationContext context, While semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.WHILE__GUARD) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.WHILE__GUARD));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.WHILE__BLOCK) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.WHILE__BLOCK));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getWhileAccess().getGuardExpressionParserRuleCall_2_0(), semanticObject.getGuard());
		feeder.accept(grammarAccess.getWhileAccess().getBlockBlockParserRuleCall_4_0(), semanticObject.getBlock());
		feeder.finish();
	}
	
	
	/**
	 * Contexts:
	 *     Expression returns Xor
	 *     Or returns Xor
	 *     Or.Or_1_0 returns Xor
	 *     Xor returns Xor
	 *     Xor.Xor_1_0 returns Xor
	 *     And returns Xor
	 *     And.And_1_0 returns Xor
	 *     Equality returns Xor
	 *     Equality.Equality_1_0 returns Xor
	 *     Relation returns Xor
	 *     Relation.Relation_1_0 returns Xor
	 *     Addition returns Xor
	 *     Addition.Addition_1_0 returns Xor
	 *     Multiplication returns Xor
	 *     Multiplication.Multiplication_1_0 returns Xor
	 *     Primary returns Xor
	 *
	 * Constraint:
	 *     (left=Xor_Xor_1_0 right=And)
	 */
	protected void sequence_Xor(ISerializationContext context, Xor semanticObject) {
		if (errorAcceptor != null) {
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.XOR__LEFT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.XOR__LEFT));
			if (transientValues.isValueTransient(semanticObject, JavaliPackage.Literals.XOR__RIGHT) == ValueTransient.YES)
				errorAcceptor.accept(diagnosticProvider.createFeatureValueMissing(semanticObject, JavaliPackage.Literals.XOR__RIGHT));
		}
		SequenceFeeder feeder = createSequencerFeeder(context, semanticObject);
		feeder.accept(grammarAccess.getXorAccess().getXorLeftAction_1_0(), semanticObject.getLeft());
		feeder.accept(grammarAccess.getXorAccess().getRightAndParserRuleCall_1_2_0(), semanticObject.getRight());
		feeder.finish();
	}
	
	
}
