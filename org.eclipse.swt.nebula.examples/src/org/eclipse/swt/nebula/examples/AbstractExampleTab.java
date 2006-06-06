/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    cgross - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.swt.nebula.examples;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import java.util.ArrayList;
import java.util.Iterator;


/**
 * The base class for all Nebula example extensions.  Extenders will need to provide implementation
 * for <code>createParameters</code> and <code>createControl</code>.
 * 
 * Extenders may also require the use of following methods:
 * <ul>
 * <li><code>recreateExample</code></li>
 * <li><code>relayoutExample</code></li>
 * <li><code>addEventParticipant</code></li>
 * </ul>
 *
 * @author cgross
 */
public abstract class AbstractExampleTab
{
    public static int[] eventIds = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,
                                       24,25,26,27,28,29,30,31,32,33,34,35,36,37};
    public static String[] eventNames = new String[]{"KeyDown","KeyUp","MouseDown","MouseUp","MouseMove",
                                               "MouseEnter","MouseExit","MouseDoubleClick","Paint",
                                               "Move","Resize","Dispose","Selection",
                                               "DefaultSelection","FocusIn","FocusOut","Expand",
                                               "Collapse","Iconify","Deiconify","Close","Show",
                                               "Hide","Modify","Verify","Activate","Deactivate",
                                               "Help","DragDetect","Arm","Traverse","MouseHover",
                                               "HardKeyDown","HardKeyUp","MenuDetect","SetData",
                                               "MouseWheel"};
    
    private ArrayList selectedEvents = new ArrayList();
    private ArrayList additionalEventParticipants = new ArrayList();
    
    private Listener listenerThatPrints;
    
    
    private GridData controlGridData = new GridData();
    private Composite controlArea;
    private Control controlExample;
    
    private boolean vFill = false;
    private boolean hFill = false;
    private Button listen;
    

    /**
     * Create the parameters section that will display to the right of the example widget.  
     * 
     * @param parent the parent composite.
     */
    public abstract void createParameters(Composite parent);

    /**
     * Create the control, based upon the selections made on the widgets created in 
     * <code>createParameters</code>.
     * 
     * @param parent the parent composite.
     * @return the example control.
     */
    public abstract Control createControl(Composite parent);

    /**
     * Recreates the example control.  This method will call <code>createControl</code>.  Extenders 
     * should call this method after a user changes an option or style on the control that will
     * require the control to be recreated.
     */
    protected void recreateExample()
    {
        if (controlExample != null && !controlExample.isDisposed())
            controlExample.dispose();
        
        controlExample = createControl(controlArea);
        
        updateListeners();
        
        controlExample.setLayoutData(controlGridData);
        
        controlArea.layout(true);
    }
    
    /**
     * Recalculates and repositions the layout of the example control based upon the layout options
     * chosen by the user.  Extenders may wish to call this method if a user changes a widget 
     * parameter that might affect the layout (usually the preferred size).
     */
    protected void relayoutExample()
    {
        controlGridData.verticalAlignment = vFill ? SWT.FILL : SWT.BEGINNING;
        controlGridData.grabExcessVerticalSpace = vFill;

        controlGridData.horizontalAlignment = hFill ? SWT.FILL : SWT.BEGINNING;
        controlGridData.grabExcessHorizontalSpace = hFill;
        
        controlExample.setLayoutData(controlGridData);
        controlArea.layout(true,true);
    }
    
    /**
     * Creates the main container and default example options (such as event listening and size 
     * options).  This method should not be called by extenders.
     * 
     * @param parent
     */
    public final void create(Composite parent)
    {
        GridLayout gl = new GridLayout(2,false);
        parent.setLayout(gl);
        
        controlArea = new Composite(parent,SWT.NONE);
        controlArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        controlArea.setLayout(new GridLayout());
        
        Group paramsGroup = new Group(parent,SWT.SHADOW_ETCHED_IN);
        paramsGroup.setText("Parameters");
        paramsGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        paramsGroup.setLayout(new GridLayout());
        
        Composite paramsArea = new Composite(paramsGroup,SWT.NONE);
        createParameters(paramsArea);
        
        Group sizeGroup = new Group(paramsGroup,SWT.SHADOW_ETCHED_IN);
        sizeGroup.setText("Size");
        
        createSize(sizeGroup);
        
        Group listenersGroup = new Group(parent,SWT.SHADOW_ETCHED_IN);
        listenersGroup.setText("Listeners");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        listenersGroup.setLayoutData(gd);
        
        createListeners(listenersGroup);
        
        recreateExample();
    }
    
    /**
     * @param parent
     */
    private void createSize(Composite parent)
    {
        parent.setLayout(new GridLayout());
        
        Button prefSize = new Button(parent, SWT.RADIO);
        prefSize.setText("Preferred");
        prefSize.setSelection(true);
        prefSize.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                controlGridData = new GridData();
                relayoutExample();
            }
        });

        Button tenSize = new Button(parent, SWT.RADIO);
        tenSize.setText("10 X 10");
        tenSize.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                controlGridData = new GridData(10,10);
                relayoutExample();
            }
        });

        Button fiftySize = new Button(parent, SWT.RADIO);
        fiftySize.setText("50 X 50");
        fiftySize.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                controlGridData = new GridData(50,50);
                relayoutExample();
            }
        });

        Button hundredSize = new Button(parent, SWT.RADIO);
        hundredSize.setText("100 X 100");
        hundredSize.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                controlGridData = new GridData(100,100);
                relayoutExample();
            }
        });

        final Button hFillB = new Button(parent, SWT.CHECK);
        hFillB.setText("Horizontal Fill");
        hFillB.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                hFill = hFillB.getSelection();
                relayoutExample();
            }
        });

        final Button vFillB = new Button(parent, SWT.CHECK);
        vFillB.setText("Vertical Fill");
        vFillB.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                vFill = vFillB.getSelection();
                relayoutExample();
            }
        });
    }
    
    private void createListeners(Composite parent)
    {
        parent.setLayout(new GridLayout(4,false));
        
        Button selectListeners = ButtonFactory.create(parent,SWT.PUSH,"Select Listeners",new Listener()
        {
            public void handleEvent(Event event)
            {
                ListenersDialog dialog = new ListenersDialog(Display.getCurrent().getActiveShell());
                
                if (dialog.open(selectedEvents) == Dialog.OK)
                    updateListeners();
            }        
        });
        
        listen = ButtonFactory.create(parent,SWT.CHECK,"Listen",new Listener()
        {
            public void handleEvent(Event event)
            {
                updateListeners();
            }        
        });
        
        Label spacer = new Label(parent,SWT.NONE);
        spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        Button clear = new Button(parent,SWT.PUSH);
        clear.setText("Clear");
        
        final Text eventText = new Text(parent,SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 120;
        gd.horizontalSpan = 4;
        eventText.setLayoutData(gd);
        
        clear.addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                eventText.setText("");
            }
        });
        
        listenerThatPrints = new Listener()
        {
            public void handleEvent(Event event)
            {
                TypedEvent typedEvent = getTypedEvent(event);
                eventText.append("\n" + typedEvent.toString());
            }        
        };
    }
    
    private void updateListeners()
    {        
        for (int i = 0; i < eventIds.length; i++)
        {
            int eventId = eventIds[i];
            boolean selected = (selectedEvents.contains(new Integer(eventId)));
            
            controlExample.removeListener(eventId, listenerThatPrints);
            if (selected && listen.getSelection())
                controlExample.addListener(eventId, listenerThatPrints);
            
            for (Iterator iterator = additionalEventParticipants.iterator(); iterator.hasNext();)
            {
                Widget participant = (Widget)iterator.next();
                
                participant.removeListener(eventId, listenerThatPrints);
                if (selected && listen.getSelection())
                    participant.addListener(eventId, listenerThatPrints);
            }            
        }
    }

    /**
     * Adds the given widget to the list of which will participate in the event
     * listening mechanism of the example tab.  In other words, if a user has chosen to listen for a
     * specific event, and that event is fired on the given widget, the event's details will print 
     * in the event text area in the example.  
     * 
     * This method is primarily used to include a widget's <code>Item</code> children in the event
     * listening mechanism.
     * 
     * @param widget
     */
    protected void addEventParticipant(final Widget widget)
    {
        additionalEventParticipants.add(widget);
        widget.addDisposeListener(new DisposeListener()
        {
            public void widgetDisposed(DisposeEvent e)
            {
                additionalEventParticipants.remove(widget);
            }        
        });
    }
    
    private TypedEvent getTypedEvent(Event e)
    {
        TypedEvent typedEvent = null;
        
        switch (e.type)
        {
            case SWT.KeyDown: case SWT.KeyUp:
                typedEvent = new KeyEvent(e);
                break;

            case SWT.MouseUp: case SWT.MouseDown: case SWT.MouseDoubleClick: case SWT.MouseMove:
            case SWT.MouseEnter: case SWT.MouseExit: case SWT.MouseHover:
                typedEvent = new MouseEvent(e);
                break;
                
            case SWT.Paint:
                typedEvent = new PaintEvent(e);
                break;
                
            case SWT.Move: case SWT.Resize:
                typedEvent = new ControlEvent(e);
                break;
                
            case SWT.Dispose:
                typedEvent = new DisposeEvent(e);
                break;
                
            case SWT.Selection: case SWT.DefaultSelection:
                typedEvent = new SelectionEvent(e);
                break;
                
            case SWT.FocusIn: case SWT.FocusOut:
                typedEvent = new FocusEvent(e);
                break;
                
            case SWT.Expand: case SWT.Collapse:
                typedEvent = new TreeEvent(e);
                break;
                
            case SWT.Iconify: case SWT.Deiconify: case SWT.Close: case SWT.Show: case SWT.Hide:
            case SWT.Activate: case SWT.Deactivate:
                typedEvent = new ShellEvent(e);
                break;
                
            case SWT.Modify:
                typedEvent = new ModifyEvent(e);
                break;
                
            case SWT.Verify:
                typedEvent = new VerifyEvent(e);
                break;
                
            case SWT.Help:
                typedEvent = new HelpEvent(e);
                break;
                
            case SWT.Arm:
                typedEvent = new ArmEvent(e);
                break;
                
            case SWT.Traverse:
                typedEvent = new TraverseEvent(e);
                break;
                
            default :
                typedEvent = new TypedEvent(e);
                break;
        }
        
        return typedEvent;
    }

}
