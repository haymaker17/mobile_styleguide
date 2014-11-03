//
//  HotelSearchResultsFilterVC.m
//  ConcurMobile
//
//  Created by Shifan Wu on 7/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelSearchResultsFilterVC.h"
#import "URBSegmentedControl.h"

@interface HotelSearchResultsFilterVC ()

@property (strong, nonatomic) IBOutlet UIView *viewStarRating;
@property (strong, nonatomic) IBOutlet UIView *viewDistance;
@property (strong, nonatomic) IBOutlet UITextField *textField;

@end

NSString *const kAllFilterItems = @"ALL";
NSString *const kMilesFilter5 = @"5 miles";
NSString *const kMilesFilter15 = @"15 miles";
NSString *const kMilesFilter25 = @"25 miles";
NSString *const kStarRatingImage3 = @"hotel_star_rating3";
NSString *const kStarRatingImage4 = @"hotel_star_rating4";
NSString *const kStarRatingImage5 = @"hotel_star_rating5";

@implementation HotelSearchResultsFilterVC

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    [self setUpDistance];
    [self setUpStarRating];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    // register for keyboard notifications
    [self registerForKeyboardNotifications];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    // unregister for keyboard notifications while not visible.
    [self removeKeyboardNotifications];
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

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (void)setUpDistance
{
    NSArray *distanceTitles = [NSArray arrayWithObjects:kAllFilterItems, kMilesFilter5, kMilesFilter15, kMilesFilter25, nil];

    URBSegmentedControl *control = [[URBSegmentedControl alloc] initWithItems:distanceTitles];
	control.frame = CGRectMake(0.0, 56.0, 320.0, 73.0);
    
    control.baseColor = [UIColor whiteColor];
    control.strokeColor = [UIColor colorWithRed:205.0 green:214.0 blue:221.0 alpha:1.0];
    control.strokeWidth = 1.0;
    control.cornerRadius = 0.0;
    control.segmentEdgeInsets = UIEdgeInsetsMake(1.0, 5.0, 9.0, 13.0);

	control.segmentBackgroundColor = [UIColor concurBlueColor];
    control.showsGradient = NO;

	[self.viewDistance addSubview:control];

	// UIKit method of handling value changes
	[control addTarget:self action:@selector(handleSelection:) forControlEvents:UIControlEventValueChanged];
	// block-based value change handler
	[control setControlEventBlock:^(NSInteger index, URBSegmentedControl *segmentedControl) {
		NSLog(@"URBSegmentedControl: control block - index=%i", index);
        switch (index) {
            case 0:
                ;
                break;
            case 1:
                ;
                break;
            case 2:
                ;
                break;
            case 3:
                ;
                break;
            default:
                break;
        }
        
	}];
}

- (void)setUpStarRating
{
    NSArray *titles = [NSArray arrayWithObjects:kAllFilterItems, @"3+", @"4+", @"5+", nil];
    NSArray *icons = [NSArray arrayWithObjects:[UIImage imageNamed:kStarRatingImage3], [UIImage imageNamed:kStarRatingImage3], [UIImage imageNamed:kStarRatingImage4], [UIImage imageNamed:kStarRatingImage5], nil];
    URBSegmentedControl *control = [[URBSegmentedControl alloc] initWithTitles:titles icons:icons];
    control.frame = CGRectMake(0.0, 56.0, 320.0, 73.0);
	control.showsGradient = NO;
    control.segmentViewLayout = URBSegmentViewLayoutVertical;
	control.contentEdgeInsets = UIEdgeInsetsMake(6.0, 6.0, 6.0, 6.0);
	control.imageEdgeInsets = UIEdgeInsetsMake(6.0, 8.0, 8.0, 8.0);
	control.titleEdgeInsets = UIEdgeInsetsZero;
    [self.viewStarRating addSubview:control];
    
	// UIKit method of handling value changes
	[control addTarget:self action:@selector(handleSelection:) forControlEvents:UIControlEventValueChanged];
	// block-based value change handler
	[control setControlEventBlock:^(NSInteger index, URBSegmentedControl *segmentedControl) {
		NSLog(@"URBSegmentedControl: control block - index=%i", index);
        switch (index) {
            case 0:
                ;
                break;
            case 1:
                ;
                break;
            case 2:
                ;
                break;
            case 3:
                ;
                break;
            default:
                break;
        }
        
	}];
}

- (IBAction)btnDonePressed:(id)sender
{
    DLog(@"done pressed");
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (IBAction)btnResetPressed:(id)sender
{
    DLog(@"Reset pressed");
}

- (void)handleSelection:(id)sender {
	NSLog(@"URBSegmentedControl: value changed");
}


#pragma - mark Keyboard Notifications
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

//-(void)textFieldDidBeginEditing:(UITextField *)sender
//{
//    if ([sender isEqual:_textField])
//    {
//        //move the main view, so that the keyboard does not hide it.
//        if  (self.view.frame.origin.y >= 0)
//        {
//            [self setViewMovedUp:YES offset:<#(CGSize)#>];
//        }
//    }
//}

//method to move the view up/down whenever the keyboard is shown/dismissed
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

-(IBAction)textFieldReturn:(id)sender
{
    [sender resignFirstResponder];
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    
    UITouch *touch = [[event allTouches] anyObject];
    if ([_textField isFirstResponder] && [touch view] != _textField) {
        [_textField resignFirstResponder];
    }
    [super touchesBegan:touches withEvent:event];
}
@end
