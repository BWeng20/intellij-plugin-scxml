package com.bw.modelthings.fsm.ui;

/**
 * Base for all platform editor UI.
 * Editor UIs shall only implement a minimalistic API to set and retrieve fields that are edited.
 * All other logic shall be placed in platform independent classes that call the UI-classes only to
 * set/get the fields.
 * @param <C> The common base class of the UI framework used.
 */
public interface EditorUI<C>
{
	C getComponent();

}
