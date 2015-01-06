//
//  HotelRoomReserveViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 8/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelRoomReserveViewController.h"
#import "HotelRoomReserveView.h"
#import "AnalyticsTracker.h"

@interface HotelRoomReserveViewController ()

// the UIScrollView allows the bottom slider to float with the screen as in spec.
// Wanny says there's a simpler way to get the same effect, but he's on vacation and I don't recall the details.
@property (nonatomic, readwrite, weak) IBOutlet UIScrollView *scrollView;

// currently active field
// https://developer.apple.com/library/ios/documentation/StringsTextFonts/Conceptual/TextAndWebiPhoneOS/KeyboardManagement/KeyboardManagement.html
@property (nonatomic, readwrite, weak) UIView *activeField;

@property (nonatomic, readonly, strong) HotelRoomReserveView *reserveView;

// TODO: make the slide to reserve button animate
// need to show amount on slide to reserve button
@property (nonatomic, readwrite, weak) IBOutlet UILabel *amountLabel;

// data we need to reserve
@property (nonatomic, readonly, weak) CTEHotelRate *selectedRate;
@property (nonatomic, strong) HotelSearchCriteriaV2 *searchCriteria;
@end

@implementation HotelRoomReserveViewController

- (id)initWithSelectedRate:(CTEHotelRate *)selectedRate searchCriteria:(HotelSearchCriteriaV2 *)searchCriteria
{
    self = [super initWithNibName:@"HotelRoomReserveViewController" bundle:nil];
    if (self) {
        _selectedRate = selectedRate;
        _searchCriteria = searchCriteria;
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self registerForKeyboardNotifications];

    [self loadReserveScreen];
    [self updateAmountLabel];
    
    // As the previous VC has no title, we provide a custom title for the back button
    UIBarButtonItem *barButton = [[UIBarButtonItem alloc] init];
    barButton.title = [@"Rooms" localize];
    self.navigationController.navigationBar.topItem.backBarButtonItem = barButton;

    [AnalyticsTracker initializeScreenName:@"Reserve Screen"];
}

- (void)registerForKeyboardNotifications
{
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWasShown:) name:UIKeyboardDidShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillBeHidden:) name:UIKeyboardWillHideNotification object:nil];
}

- (void)keyboardWasShown:(NSNotification*)aNotification
{
    // add space for keyboard.  this is necessary if the active area is at the bottom of the view
    NSDictionary* info = [aNotification userInfo];
    CGSize kbSize = [[info objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;

    UIEdgeInsets contentInsets = UIEdgeInsetsMake(0.0, 0.0, kbSize.height, 0.0);
    self.scrollView.contentInset = contentInsets;
    self.scrollView.scrollIndicatorInsets = contentInsets;

    // get the field rectangle in relation to the scrollview and scroll to it
    CGRect fieldRectRelativeToThisView = [self.activeField.superview convertRect:self.activeField.frame toView:self.scrollView];
    [self.scrollView scrollRectToVisible:fieldRectRelativeToThisView animated:YES];
}

- (void)keyboardWillBeHidden:(NSNotification*)aNotification
{
    // TODO: animate this transition to make is nicer
    UIEdgeInsets contentInsets = UIEdgeInsetsZero;
    self.scrollView.contentInset = contentInsets;
    self.scrollView.scrollIndicatorInsets = contentInsets;
}

// load subview into scrollview programmatically
- (void)loadReserveScreen
{
    __weak HotelRoomReserveViewController *weakSelf = self;
    _reserveView = [[[NSBundle mainBundle] loadNibNamed: @"HotelRoomReserveView" owner: self options: nil] objectAtIndex:0];
    if (self.reserveView) {
        [self.reserveView setSelectedRate:self.selectedRate searchCriteria:self.searchCriteria nextViewControllerBlock:^(UIViewController *nextViewController) {
            // this callback block pushes any screens that need to be pushed by the reserveView
            if (nextViewController) {
                [weakSelf.navigationController pushViewController:nextViewController animated:YES];
            }
        } updateActiveField:^(UIView *activeField) {
            // this callback block sets the activeField so any keyboards will not be obscured
            if (activeField) {
                weakSelf.activeField = activeField;
            }
        }];
        [self.scrollView addSubview:self.reserveView];

        // update the content size of the scrollview to the reserve view size
        self.scrollView.contentSize = self.reserveView.frame.size;
    }
}

- (void)updateAmountLabel
{
    NSString *amount = [FormatUtils formatMoneyString:self.selectedRate.totalAmount crnCode:self.selectedRate.currency decimalPlaces:0];
    [self.amountLabel setText:amount];
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
}

- (IBAction)reserve
{
    [self.reserveView reserve];
    [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Slide to Reserve" eventLabel:nil eventValue:nil];
}

@end
