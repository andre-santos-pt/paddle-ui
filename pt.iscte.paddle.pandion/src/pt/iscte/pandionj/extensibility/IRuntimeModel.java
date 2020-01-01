package pt.iscte.pandionj.extensibility;

import java.util.List;

public interface IRuntimeModel extends IObservableModel<IRuntimeModel.Event<IStackFrameModel>> {

	public class Event<T> {
		public enum Type {
			NEW_FRAME, NEW_STACK, REMOVE_FRAME, STEP, EVALUATION, GARBAGE_COLLECTION, TERMINATION;
		}

		public final Type type;
		public final T arg;

		public Event(Type type, T arg) {
			this.type = type;
			this.arg = arg;
		}
	}
	
	IStackFrameModel getTopFrame();
	List<IStackFrameModel> getActiveCallStack();
	
	IEntityModel getObject(Object obj, boolean loose, IReferenceModel model);
	List<IReferenceModel> findReferences(IEntityModel e); 
	
	void evaluationNotify(); //throws DebugException;
	boolean isTerminated();
	void setTerminated();
}
