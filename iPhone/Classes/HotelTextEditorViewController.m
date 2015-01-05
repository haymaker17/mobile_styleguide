//
//  HotelTextEditorViewController.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "HotelTextEditorViewController.h"
#import "ExSystem.h" 


@interface HotelTextEditorViewController()
@end

@implementation HotelTextEditorViewController

@synthesize fromView;
@synthesize customTitle;
@synthesize textField, enteredText;
@synthesize parentVC;

-(IBAction)didEndOnExit:(id)sender
{
    // Removed any actions from here. DONE is now handled by the function below
}

- (BOOL)textFieldShouldReturn:(UITextField *)field {
    [field resignFirstResponder];
    // set instance flag to indicate that DONE has been clicked
    self.pressedDone = YES;
    [self closeView];
    return YES;
}

-(void)closeView
{
	NSString* text = (textField.text == nil ? @"" : textField.text);
	NSMutableDictionary *pBag = [[NSMutableDictionary alloc] initWithObjectsAndKeys:text, @"TEXT", @"YES", @"DONTPUSHVIEW", @"YES", @"POPTOVIEW", @"YES", @"SHORT_CIRCUIT", self.fromIndexPath, @"FROM_INDEX_PATH", nil];
    
    //NSLog(@"fromView = %@", fromView);
    
    // Generate the message once
    Msg *msg = [[Msg alloc] init];
    msg.idKey = @"SHORT_CIRCUIT";
    msg.parameterBag = pBag;

    if (parentVC != nil)
    {
        // new logic, will use a reference to the parent VC if set
        [parentVC respondToFoundData:msg];
        if (self.pressedDone)
        {
            // Only pop the VC from the stack if DONE was pressed
            [self.navigationController popViewControllerAnimated:YES];
        }
    }
    else
    {
        // existing logic, will check VC entries on stack
        if([fromView isEqualToString:@"HOTEL"] || [fromView isEqualToString:@"HOTEL_BOOKING"] || [fromView isEqualToString:@"CAR_DETAILS" ] || [fromView isEqualToString:@"AIR_BOOKING"] || [fromView isEqualToString:@"TRAIN_DETAILS"])
        {
            NSUInteger vcCount = [self.navigationController.viewControllers count];
            UIViewController *vc = [self.navigationController.viewControllers lastObject] != self ? [self.navigationController.viewControllers lastObject] : (self.navigationController.viewControllers)[vcCount - 2];
            MobileViewController *mvc = (MobileViewController *)vc;
            [mvc respondToFoundData:msg];
            if (self.pressedDone)
            {
                // Only pop the VC from the stack if DONE was pressed
                [self.navigationController popViewControllerAnimated:YES];
            }
        }
        else 
        {
            UIViewController *vc = (self.navigationController.viewControllers)[0];
            MobileViewController *mvc = (MobileViewController *)vc;
            [mvc respondToFoundData:msg];
            if (self.pressedDone)
            {
                // Only pop the VC from the stack if DONE was pressed
                [self.navigationController popToRootViewControllerAnimated:YES];
            }
        }
    }
}

#pragma mark -
#pragma mark MobileViewController Methods
-(NSString *)getViewIDKey
{
	return HOTEL_TEXT_EDITOR;
}

-(NSString *)getViewDisplayType
{
	return VIEW_DISPLAY_TYPE_NAVI;
}


-(void)respondToFoundData:(Msg *)msg
{
	if ([msg.idKey isEqualToString:@"SHORT_CIRCUIT"] && msg.parameterBag != nil)
	{
		self.fromView = (NSString*)(msg.parameterBag)[@"FROM_VIEW"];
		
        if ((msg.parameterBag)[@"TITLE"] != nil)
        {
            customTitle = (NSString*)(msg.parameterBag)[@"TITLE"];
            self.title = customTitle;
        }
        
		if ((msg.parameterBag)[@"TEXT"] != nil)
		{
            //MOB-12288
			if (enteredText != nil)
				enteredText = (NSString*)(msg.parameterBag)[@"TEXT"];
            else
                enteredText = [[NSString alloc] initWithString:(NSString*)(msg.parameterBag)[@"TEXT"]];
            textField.text = enteredText;
		}
		
		if ((msg.parameterBag)[@"PLACEHOLDER"] != nil)
		{
			NSString *placeholder = (NSString*)(msg.parameterBag)[@"PLACEHOLDER"];
            self.placeholderText = placeholder;
			textField.placeholder = placeholder;
		}
	}
}

#pragma mark -
#pragma mark View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	// Add custom background
	/*
	UIImage *backgroundImage = [UIImage imageNamed:@"Breeze_Gradient.png"];
	UIImageView *backgroundImageView = [[UIImageView alloc] initWithImage:backgroundImage];
	[self.view addSubview:backgroundImageView];
	[self.view sendSubviewToBack:backgroundImageView];
	[backgroundImageView release];
	*/
	self.view.backgroundColor = [UIColor whiteColor];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

	if (customTitle != nil)
		self.title = customTitle;
    //MOB-12288
    if (enteredText != nil)
        self.textField.text = enteredText;
    
    if (self.placeholderText) {
        self.textField.placeholder = self.placeholderText;
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
	[textField becomeFirstResponder];
}

// function added to catch when the back navigation button is used
- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    if (!self.pressedDone)
    {
        // This method gets triggered by closeView being called, we can avoid calling closeView again
        // by checking the flag to see if DONE has been pressed
        [self closeView];
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation {
    // Return YES for supported orientations
    if([UIDevice isPad])
        return YES;
    else
        return NO;
}

#pragma mark -
#pragma mark Memory management

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Relinquish ownership any cached data, images, etc that aren't in use.
}

- (void)viewDidUnload
{
    // Relinquish ownership of anything that can be recreated in viewDidLoad or on demand.
    // For example: self.myOutlet = nil;
}




@end

