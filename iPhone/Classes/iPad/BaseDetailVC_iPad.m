//
//  BaseDetailVC_iPad.m
//  ConcurMobile
//
//  Created by charlottef on 3/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "BaseDetailVC_iPad.h"

@implementation ButtonDescriptor
@synthesize buttonId, title;

+ (ButtonDescriptor*) buttonDescriptorWithId:(NSString*)btnId title:(NSString*)btnTitle
{
    ButtonDescriptor* descriptor = [[ButtonDescriptor alloc] init];
    descriptor.buttonId = btnId;
    descriptor.title = btnTitle;
    return descriptor;
}

@end

#define MAX_BUTTONS 6

@interface BaseDetailVC_iPad ()
{
    NSArray *_buttonDescriptors;
}

// Left and right panes
@property (strong, nonatomic) IBOutlet UIView       *leftPaneView;
@property (strong, nonatomic) IBOutlet UIView       *leftPaneButtonContainerView;

// Buttons
@property (strong, nonatomic) IBOutlet UIButton     *button0;
@property (strong, nonatomic) IBOutlet UIButton     *button1;
@property (strong, nonatomic) IBOutlet UIButton     *button2;
@property (strong, nonatomic) IBOutlet UIButton     *button3;
@property (strong, nonatomic) IBOutlet UIButton     *button4;
@property (strong, nonatomic) IBOutlet UIButton     *button5;

// Button Labels
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton0;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton1;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton2;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton3;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton4;
@property (strong, nonatomic) IBOutlet UILabel      *labelOnButton5;

- (IBAction) buttonPressed:(id)sender;

@end

@implementation BaseDetailVC_iPad

@synthesize leftPaneHeaderView = _leftPaneHeaderView;
@synthesize leftPaneFooterView = _leftPaneFooterView;

- (void)viewWillLayoutSubviews
{
    // This method is called whenever view layout normally happens, e.g. the view is loaded, the device is rotated, etc.
    
    float viewWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 1024.0 : 768.0;
    float viewHeight = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 760.0 : 1016.0;
    
    // Layout the left pane
    float newPaneWidth = UIInterfaceOrientationIsLandscape(self.interfaceOrientation) ? 300.0 : 246.0;
    CGRect newPaneFrame = CGRectMake(0, 0, newPaneWidth, viewHeight);
    self.leftPaneView.frame = newPaneFrame;
    
    // Layout the right table
    const float widthOfBorderAroundTable = 12.0;
    float newTableLeft = newPaneWidth + widthOfBorderAroundTable; // Account for left border
    float newTableTop = widthOfBorderAroundTable; // Account for top border
    float newTableWidth = viewWidth - newPaneWidth - widthOfBorderAroundTable - widthOfBorderAroundTable; // Account for left and right borders
    float newTableHeight = (viewHeight - newTableTop) - 100; // Tablelist doesn't need the extra 100 that the left panel does
    CGRect newTableFrame = CGRectMake(newTableLeft, newTableTop, newTableWidth, newTableHeight);
    self.rightTableView.frame = newTableFrame;
    
    // Layout the pane header view
    if (self.leftPaneHeaderView != nil)
    {
        for (UIView *subview in self.leftPaneView.subviews)
        {
            if (subview == self.leftPaneHeaderView)
            {
                CGRect newHeaderFrame = CGRectMake(0, 0, self.leftPaneView.frame.size.width, self.leftPaneHeaderView.frame.size.height);
                self.leftPaneHeaderView.frame = newHeaderFrame;
            }
        }
     }
    
    // Layout the pane footer view
    if (self.leftPaneFooterView != nil)
    {
        for (UIView *subview in self.leftPaneView.subviews)
        {
            if (subview == self.leftPaneFooterView)
            {
                CGRect newFooterFrame = CGRectMake(0, self.leftPaneView.frame.size.height - self.leftPaneFooterView.frame.size.height, newPaneWidth, self.leftPaneFooterView.frame.size.height);
                self.leftPaneFooterView.frame = newFooterFrame;
            }
        }
    }
    
    // Layout the pane button container view
    CGRect newButtonContainerFrame = CGRectMake(0, self.leftPaneHeaderView.frame.size.height, newPaneWidth, viewHeight - self.leftPaneHeaderView.frame.size.height);
    self.leftPaneButtonContainerView.frame = newButtonContainerFrame;
    
    // Fix the loading view's label and activity indicator
    if (UIInterfaceOrientationIsPortrait(self.interfaceOrientation))
    {
        self.loadingLabel.frame = CGRectMake(39.0, 400.0, 688.0, 120.0);
        self.activityView.frame = CGRectMake(365.0, 525.0, 37.0, 37.0);
    }
    else
    {
        self.loadingLabel.frame = CGRectMake(157.0, 272.0, 688.0, 120.0);
        self.activityView.frame = CGRectMake(493.0, 397.0, 37.0, 37.0);
    }
    
    // Layout everything else
    [super viewWillLayoutSubviews];
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // Just add the header view. Leave the layout to viewWillLayoutSubviews
    _leftPaneHeaderView = [self loadHeaderView];
    [self.leftPaneView addSubview:_leftPaneHeaderView];
    
    // Just add the footer view. Leave the layout to viewWillLayoutSubviews
    _leftPaneFooterView = [self loadFooterView];
    [self.leftPaneView addSubview:_leftPaneFooterView];
    
    // Tags will be used to identify buttons.  See buttonPressed method.
    self.button0.tag = 0;
    self.button1.tag = 1;
    self.button2.tag = 2;
    self.button3.tag = 3;
    self.button4.tag = 4;
    self.button5.tag = 5;
    
    [self configureButtons];
}

#pragma mark - Button Methods
- (IBAction) buttonPressed:(id)sender
{
    UIButton *button = (UIButton*)sender;
    int buttonIndex = button.tag;
    
    if (buttonIndex < _buttonDescriptors.count)
    {
        ButtonDescriptor *descriptor = _buttonDescriptors[buttonIndex];
        CGRect buttonRect = [self.view convertRect:button.frame fromView:self.leftPaneButtonContainerView];
        [self didPressButtonAtIndex:buttonIndex withId:descriptor.buttonId inRect:buttonRect];
    }
}

- (UIButton*) buttonAtIndex:(int)index
{
    if (index == 0)
        return self.button0;
    else if (index == 1)
        return self.button1;
    else if (index == 2)
        return self.button2;
    else if (index == 3)
        return self.button3;
    else if (index == 4)
        return self.button4;
    else if (index == 5)
        return self.button5;
    else
        return nil;
}

- (UILabel*) labelOnButtonAtIndex:(int)index
{
    if (index == 0)
        return self.labelOnButton0;
    else if (index == 1)
        return self.labelOnButton1;
    else if (index == 2)
        return self.labelOnButton2;
    else if (index == 3)
        return self.labelOnButton3;
    else if (index == 4)
        return self.labelOnButton4;
    else if (index == 5)
        return self.labelOnButton5;
    else
        return nil;
}

- (void) configureButtons
{
    if (!self.isViewLoaded || _buttonDescriptors == nil)
        return;
    
    for (int buttonIndex = 0; buttonIndex < _buttonDescriptors.count; buttonIndex++)
    {
        ButtonDescriptor *descriptor = _buttonDescriptors[buttonIndex];
        
        UIButton *button = [self buttonAtIndex:buttonIndex];
        button.hidden = NO;
        
        UILabel *labelForButton = [self labelOnButtonAtIndex:buttonIndex];
        labelForButton.text = descriptor.title;
        labelForButton.hidden = NO;
    }
    
    for (int unusedButtonIndex = _buttonDescriptors.count; unusedButtonIndex < MAX_BUTTONS; unusedButtonIndex++)
    {
        UIButton *unusedButton = [self buttonAtIndex:unusedButtonIndex];
        unusedButton.hidden = YES;
        UILabel *labelForButton = [self labelOnButtonAtIndex:unusedButtonIndex];
        labelForButton.hidden = YES;
    }
}

#pragma mark - Methods for Subclasses to Call
- (void) setButtonDescriptors:(NSArray*)descriptors
{
    // This method can be called any time and the UI will be updated to show the new buttons
    
    // This method makes its own immutable copy of the array, so if you need to change a button, then (1) arrange the buttons in your own array, and (2) pass your array to this method which will copy it.
    
    _buttonDescriptors = [NSArray arrayWithArray:descriptors]; // Make an immutable copy of the array.
    [self configureButtons];
}

#pragma mark - Methods for Subclasses to Override
- (UIView*) loadHeaderView
{
    return nil;
}

- (UIView*) loadFooterView
{
    return nil;
}
    
- (void) didPressButtonAtIndex:(int)buttonIndex withId:(NSString*)buttonId inRect:(CGRect)rect
{
}

@end
