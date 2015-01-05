//
//  HotelSearchFilterViewController.m
//  ConcurMobile
//
//  Created by Ray Chi on 9/19/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchFilterViewController.h"
#import "FilterSegmentedControlGroup.h"
#import <QuartzCore/QuartzCore.h>

@interface HotelSearchFilterViewController ()


@property (nonatomic,strong) FilterSegmentedControlGroup *ratingGroup;
@property (nonatomic,strong) FilterSegmentedControlGroup *milesGroup;

@end

@implementation HotelSearchFilterViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    //
    // register for keyboard notifications
    [self registerForKeyboardNotifications];
    
    //
    // Init Dictionary and WeakSelf to avoid Retain Cycle
    NSDictionary *preSelectedDict = [self.selectedIndexDict copy];
    self.selectedIndexDict = [[NSMutableDictionary alloc] init];
    
    __weak typeof(self) weakSelf = self;
    
    //
    // Add Rating segmented control Group
    _ratingGroup = [[FilterSegmentedControlGroup alloc] initWithFrame:CGRectMake(0, 59, 0, 0)];
    [_ratingGroup setOnSelected:^(NSInteger newIndex){
        NSString *newIndexStr = [NSString stringWithFormat:@"%li",(long)newIndex];
        [weakSelf.selectedIndexDict setValue:newIndexStr forKey:@"Rating"];
    }];
    
    [_ratingGroup changeType:1];
    [self.mainView addSubview:_ratingGroup];
    
    NSInteger *preRate = [[preSelectedDict objectForKey:@"Rating"] intValue];
    _ratingGroup.selectIndex = preRate;
    
    //
    // Add Miles segmented control Group
    _milesGroup = [[FilterSegmentedControlGroup alloc] initWithFrame:CGRectMake(0, 191, 0, 0)];
    [_milesGroup setOnSelected:^(NSInteger newIndex){
        NSString *newIndexStr = [NSString stringWithFormat:@"%li",(long)newIndex];
        [weakSelf.selectedIndexDict setValue:newIndexStr forKey:@"Miles"];
    }];
    
    [_milesGroup changeType:2];
    [self.mainView addSubview:_milesGroup];
    
    NSInteger *preMiles = [[preSelectedDict objectForKey:@"Miles"] intValue];
    _milesGroup.selectIndex = preMiles;
    
    //
    // Set up Text Filed
    self.searchTextField.layer.borderColor = [[UIColor colorWithRed:167/255.0 green:182/255.0 blue:191/255.0 alpha:0.5] CGColor];
    self.searchTextField.layer.borderWidth= 1.0f;
    
    // Add a padding to make the text field looks better
    UIView *paddingView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 10, 20)];
    self.searchTextField.leftView = paddingView;
    self.searchTextField.leftViewMode = UITextFieldViewModeAlways;
    
    if(![[preSelectedDict objectForKey:@"Text"] isEqualToString:@""]){
        self.searchTextField.text = [preSelectedDict objectForKey:@"Text"];
    }
}


- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    
    //
    // unregister for keyboard notifications while not visible.
    [self removeKeyboardNotifications];
}


#pragma mark --- Button click event

- (IBAction)btnReset_Click:(id)sender {
    DLog(@"Reset Pressed");
    self.ratingGroup.selectIndex = 0;
    self.milesGroup.selectIndex = 0;
    self.searchTextField.text = @"";
}

- (IBAction)btnDone_Click:(id)sender {
    DLog(@"Press Done");
    
    [self.selectedIndexDict setValue:self.searchTextField.text forKeyPath:@"Text"];
    [self dismissViewControllerAnimated:YES completion:^{
        if(self.FilterTracking){
            self.FilterTracking(self.selectedIndexDict);
        }
    }];
}


#pragma mark -- Keyboard Notifications and Delegate
/**
 *  method to move the view up/down whenever the keyboard is shown/dismissed
 *
 *  @param movedUp      flag for move up
 */
-(void)setViewMovedUp:(BOOL)movedUp offset:(CGSize)keyboardSize
{
    [UIView beginAnimations:nil context:NULL];
    [UIView setAnimationDuration:0.3]; // if you want to slide up the view
    
    CGRect rect = self.view.frame;
    if (movedUp)
    {
        // 1. move the view's origin up so that the text field that will be hidden come above the keyboard
        // 2. increase the size of the view so that the area behind the keyboard is covered up.
        rect.origin.y -= keyboardSize.height;
        rect.size.height += keyboardSize.height;
    }
    else
    {
        // revert back to the normal state.
        rect.origin.y += keyboardSize.height;
        rect.size.height -= keyboardSize.height;
    }
    self.view.frame = rect;
    
    [UIView commitAnimations];
}

- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillShow:)
                                                 name:UIKeyboardWillShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(keyboardWillHide:)
                                                 name:UIKeyboardWillHideNotification object:nil];
}

- (void)removeKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardWillShowNotification object:nil];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardWillHideNotification object:nil];
}

-(void)keyboardWillShow:(NSNotification *)aNotification {
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    if (self.view.frame.origin.y >= 0)
    {
        [self setViewMovedUp:YES offset:kbSize];
    }
    else if (self.view.frame.origin.y < 0)
    {
        [self setViewMovedUp:NO offset:kbSize];
    }
}

-(void)keyboardWillHide:(NSNotification *)aNotification {
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    if (self.view.frame.origin.y >= 0)
    {
        [self setViewMovedUp:YES offset:kbSize];
    }
    else if (self.view.frame.origin.y < 0)
    {
        [self setViewMovedUp:NO offset:kbSize];
    }
}


-(IBAction)textFieldReturn:(id)sender
{
    [sender resignFirstResponder];
    
    // once type on Return/Done, begin filter and go back
//    [self btnDone_Click:sender];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch *touch = [[event allTouches] anyObject];
    if ([_searchTextField isFirstResponder] && [touch view] != _searchTextField) {
        [_searchTextField resignFirstResponder];
    }
    [super touchesBegan:touches withEvent:event];
}



@end
