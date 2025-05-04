package com.bw.modelthings.fsm.ui.swing;

import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.model.Transition;
import com.bw.modelthings.fsm.model.TransitionType;
import com.bw.modelthings.fsm.ui.TransitionEditorUI;
import com.bw.modelthings.fsm.ui.TransitionVisual;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class TransitionEditor extends JPanel implements TransitionEditorUI<JComponent>
{
	JComboBox<TransitionType> _type;

	JButton _addEvent;
	JButton _removeEvent;
	JList<String> _events;

	JTextArea _condition;

	JButton _addTarget;
	JButton _removeTarget;
	JList<State> _targets;

	JTextArea _content;


	public TransitionEditor()
	{
		super(new GridBagLayout());

		_type = new JComboBox<>(TransitionType.values());

		_events = new JList<>();
		_events.setLayoutOrientation(JList.VERTICAL);
		_events.setVisibleRowCount(3);
		_addEvent = new JButton("+");
		_removeEvent = new JButton("-");
		_condition = new JTextArea();
		_condition.setRows(4);

		_targets = new JList<>();
		_targets.setLayoutOrientation(JList.VERTICAL);
		_targets.setVisibleRowCount(3);
		_addTarget = new JButton("+");
		_removeTarget = new JButton("-");

		GridBagConstraints gc = new GridBagConstraints();
		gc.anchor = GridBagConstraints.NORTHWEST;

		gc.gridy = 0;
		gc.gridx = 0;
		add(new JLabel("Type"), gc);
		gc.gridy++;
		add(new JLabel("Events"), gc);
		gc.gridy += 2;
		add(new JLabel("Condition"), gc);
		gc.gridy++;
		add(new JLabel("Targets"), gc);

		gc.gridy = 0;
		gc.insets = new Insets(0, 5, 5, 5);
		gc.weightx = 1;
		gc.weighty = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.gridx = 1;
		add(_type, gc);
		gc.gridy++;
		gc.weighty = 1;
		gc.fill = GridBagConstraints.BOTH;
		gc.insets.bottom = 0;
		add(new JScrollPane(_events), gc);
		gc.gridy += 2;
		gc.insets.bottom = 5;
		add(new JScrollPane(_condition), gc);
		gc.gridy++;
		gc.insets.bottom = 0;
		add(new JScrollPane(_targets), gc);

		gc.insets.bottom = 5;
		gc.gridy = 2;
		gc.weighty = 0;
		gc.fill = GridBagConstraints.HORIZONTAL;
		add(createListCtrl(_addEvent, _removeEvent), gc);
		gc.gridy = 5;
		add(createListCtrl(_addTarget, _removeTarget), gc);
	}

	private JPanel createListCtrl(JButton add, JButton remove)
	{
		JPanel p = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();

		gc.gridx = 0;
		gc.gridy = 0;
		gc.weightx = 1;
		gc.fill = GridBagConstraints.NONE;
		gc.anchor = GridBagConstraints.WEST;
		p.add(add, gc);
		gc.anchor = GridBagConstraints.EAST;
		gc.gridx = 1;
		p.add(remove, gc);
		return p;
	}

	@Override
	public void setTransitionVisual(TransitionVisual transition)
	{
		final Transition t = transition.getTransition();
		if (t != null)
		{
			_type.setSelectedItem(t._transitionType);

			DefaultListModel eventLm = new DefaultListModel<>();
			eventLm.addAll(t._events);
			_events.setModel(eventLm);

			_condition.setText(t._cond);

			DefaultListModel targetLm = new DefaultListModel<>();
			targetLm.addAll(t._target);
			_targets.setModel(targetLm);
		}

	}

	@Override
	public JComponent getComponent()
	{
		return this;
	}
}
