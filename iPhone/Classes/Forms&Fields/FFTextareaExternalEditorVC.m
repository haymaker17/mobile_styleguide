//
//  FFTextareaEditorVC.m
//  ConcurMobile
//
//  Created by laurent mery on 31/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFTextareaExternalEditorVC.h"
#import "FFField.h"

@interface FFTextareaExternalEditorVC() <UITextViewDelegate>

@property (nonatomic, strong) UITextView *textarea;

@end

@implementation FFTextareaExternalEditorVC

#pragma mark - init

- (void)viewDidLoad {
	
	[super viewDidLoad];
	
    [self registerForKeyboardNotifications];
    
    [self updateNavigationBar];
}

- (void)loadView {

    [super loadView];
    
    NSDictionary *metrics = @{
        @"margeLeft": @15,
        @"margeRight": @15,
        @"margeTop": @79,
        @"margeBottom": @15
    };

    
    [self setAutomaticallyAdjustsScrollViewInsets:NO];
    
    _textarea = [self createTextarea];
	[self.view addSubview:_textarea];

    NSDictionary *elements = @{
                               @"textarea": _textarea
                               };
    
    //constraint
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"H:|-margeLeft-[textarea]-margeRight-|"
                                                                      options:0
                                                                      metrics:metrics
                                                                        views:elements]];
    [self.view addConstraints:[NSLayoutConstraint constraintsWithVisualFormat:@"V:|-margeTop-[textarea]-margeBottom-|"
                                                                      options:0
                                                                      metrics:metrics
                                                                        views:elements]];
}

-(void)viewDidAppear:(BOOL)animated{
    
    [_textarea becomeFirstResponder];
    
    [super viewDidAppear:animated];
}



-(UITextView*)createTextarea{
    
    UITextView *textarea = [[UITextView alloc] init];
    
    textarea.translatesAutoresizingMaskIntoConstraints = NO;
    
    [textarea setDelegate:self];
    [textarea setText:[self.field.dataType stringValue]];
    
    [textarea setKeyboardType:UIKeyboardTypeDefault];
    [textarea setKeyboardAppearance:UIKeyboardAppearanceDefault];
    
    //border
    [textarea.layer setBorderColor:[[[UIColor grayColor] colorWithAlphaComponent:0.5] CGColor]];
    [textarea.layer setBorderWidth:1];
    
    textarea.layer.cornerRadius = 5;
    textarea.clipsToBounds = YES;
    
    return textarea;
}

#pragma mark - navigation bar

-(void)updateNavigationBar{
    
    [self.navigationItem hidesBackButton];
    UIBarButtonItem *buttonBack = [[UIBarButtonItem alloc] initWithTitle:[@"Back" localize]
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(navButtonBackTapped:)];

    
    self.navigationItem.title = [self.field label];
    
    /*
    UIBarButtonItem *buttonSave = [[UIBarButtonItem alloc] initWithTitle:[@"Save" localize]
                                                                   style:UIBarButtonItemStylePlain
                                                                  target:self
                                                                  action:@selector(navButtonSaveTapped:)];*/
    
    [self.navigationItem setLeftBarButtonItem:buttonBack];
    //[self.navigationItem setRightBarButtonItem:buttonSave animated:NO];
}


-(void)navButtonBackTapped:(id)sender{

    //update dataType
    [self.field.dataType setStringValue:_textarea.text];
    
    //return previous screen
    [self.navigationController popViewControllerAnimated:NO];

    /*
    if ([self.dataType isDirty]){
        
        // Alert to set required fields before save
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:nil
                              message:[@"RPT_SAVE_CONFIRM_MSG" localize] //Your changes may be lost if you proceed.  Would you like to save these changes?
                              delegate:self
                              cancelButtonTitle:[@"Yes" localize]
                              otherButtonTitles:[@"No" localize], nil];
        
        [alert show];
    
    }
    else {
        
        [self.navigationController popViewControllerAnimated:NO];
    }
     */
}

/*
-(void)navButtonSaveTapped:(id)sender{
    
    //update dataType
    [self.dataType setStringValue:_textarea.text];
    
    //return previous screen
    [self.navigationController popViewControllerAnimated:NO];
}


-(void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex{
    
    if (buttonIndex == 0) { // Yes - save changes
    
        [self navButtonSaveTapped:nil];
    }
    else if (buttonIndex == 1){ // No - exit without changes
    
        [self.navigationController popViewControllerAnimated:NO];
    }
}
*/

#pragma mark - keyboard managment

- (void)registerForKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(onKeyboardShow:)
                                                 name:UIKeyboardDidShowNotification
                                               object:nil];
}

- (void)deregisterFromKeyboardNotifications {
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIKeyboardDidShowNotification
                                                  object:nil];
}



- (void)onKeyboardShow:(NSNotification *)notification {
    
    NSDictionary* info = [notification userInfo];
    CGSize heightKeyBoard = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;

    [self.view setFrame:CGRectMake(self.view.frame.origin.x, self.view.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height - heightKeyBoard.height)];
    [_textarea needsUpdateConstraints];
}



#pragma mark - memory managment

-(void)dealloc{
	
	_field = nil;
    _textarea = nil;
    [self deregisterFromKeyboardNotifications];
}



@end
