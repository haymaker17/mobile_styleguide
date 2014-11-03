//
//  HotelRoomReserveView.m
//  ConcurMobile
//
//  Created by ernest cho on 8/20/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelRoomReserveView.h"
#import "HotelCancellationPolicyViewController.h"
#import "CreditCardListTableViewController.h"
#import "CTEDateUtility.h"
#import "WaitViewController.h"
#import "HotelRoomViolationView.h"
#import "AnalyticsTracker.h"

#import "HotelItineraryTransitionHack.h"

typedef void(^NextViewControllerBlock)(UIViewController *nextViewController);
typedef void(^UpdateActiveFieldBlock)(UIView *activeField);

/**
 *  This class is programmatically instantiated by HotelRoomReserveViewController
 */
@interface HotelRoomReserveView()

// data we need to reserve
@property (nonatomic, readonly, strong) CTEHotelRate *selectedRate;
@property (nonatomic, readonly, strong) CTECreditCard *selectedCreditCard;

@property (nonatomic, readonly, strong) NSArray *creditCards;
@property (nonatomic, readonly, strong) NSString *cancellationPolicy;

@property (nonatomic, readwrite, weak) IBOutlet UIImageView *image;
@property (nonatomic, readwrite, weak) IBOutlet UITextView *roomDescription;
@property (nonatomic, readwrite, weak) IBOutlet UILabel *dateRange;
@property (nonatomic, readwrite, weak) IBOutlet UILabel *numberOfNights;
@property (nonatomic, readwrite, weak) IBOutlet UILabel *price;
@property (nonatomic, readwrite, weak) IBOutlet UILabel *creditCardButton;
@property (nonatomic, readwrite, weak) IBOutlet UILabel *cancellationPolicyButton;

// need to hide this sometimes
@property (nonatomic, readwrite, weak) IBOutlet UIView *violationsHeader;

// violations views
@property (nonatomic, readonly, strong) NSArray *violationViews;

@property (nonatomic, readonly, copy) NextViewControllerBlock nextViewControllerBlock;
@property (nonatomic, readonly, copy) UpdateActiveFieldBlock updateActiveFieldBlock;

// cannot have this stupid thing get dealloc'd
@property (nonatomic, readonly, strong) HotelItineraryTransitionHack *itineraryTransitionHack;

@end

@implementation HotelRoomReserveView

// this MUST be called before pushing the screen or else the screen will be useless
- (void)setSelectedRate:(CTEHotelRate *)rate nextViewControllerBlock:(void (^)(UIViewController *nextViewController))nextViewControllerBlock updateActiveField:(void (^)(UIView *activeField))updateActiveField
{
    _selectedRate = rate;
    _nextViewControllerBlock = nextViewControllerBlock;
    _updateActiveFieldBlock = updateActiveField;

    [self fetchCreditCardsAndCancellationPolicy];

    // temporary image loading code
    // does not cache and should probably be replaced later with a utility method
    if (self.selectedRate.hotel.images.count > 0) {
        [self loadImageFromURL:[self.selectedRate.hotel.images objectAtIndex:0]];
    }

    [self.roomDescription setText:self.selectedRate.roomDescription];
    [self updateDateRange];
    [self updateNumberOfNights];
    [self updateAmountLabel];

    // TODO: verify that this is ok
    if (self.selectedRate.violations.count > 0 && self.selectedRate.mostSevereViolation.enforcementLevel != CTEHotelBookingAllowed) {
        [self addViolationSections];
    } else {
        [self.violationsHeader setHidden:YES];
    }
}

- (void)addViolationSections
{
    NSMutableArray *tmpViolationViews = [[NSMutableArray alloc] init];

    // adjust this variable by the height of the violations section in HotelRoomViolationView.xib plus a little buffer
    int violationSectionHeight = 270;
    // adjust this variable by the height of the view in HotelReserveView.xib
    int offset = self.frame.size.height;

    for (CTEHotelViolation *violation in self.selectedRate.violations) {
        HotelRoomViolationView *violationView = [[[NSBundle mainBundle] loadNibNamed: @"HotelRoomViolationView" owner: self options: nil] objectAtIndex:0];
        if (violationView) {
            [violationView setHotelViolation:violation nextViewControllerBlock:self.nextViewControllerBlock updateActiveField:self.updateActiveFieldBlock];
            violationView.frame = CGRectMake(0, offset, violationView.frame.size.width, offset + violationSectionHeight);
            [self addSubview:violationView];


            offset = offset + violationSectionHeight;

            // need the view in order to get the user selected violation reasons
            [tmpViolationViews addObject:violationView];
        }
    }
    _violationViews = tmpViolationViews;

    // update the frame size, need to do this to enable the programmatically added violation sections
    self.frame = CGRectMake(self.frame.origin.x, self.frame.origin.y, self.frame.size.width, offset);
}

- (void)updateAmountLabel
{
    // TODO: need to set currency symbol based on currency code
    NSString *amount = [NSString stringWithFormat:@"$%@", self.selectedRate.totalAmount];
    [self.price setText:amount];
}

- (void)updateNumberOfNights
{
    NSString *format = @"%d night";
    if (self.selectedRate.hotel.numberOfNights > 1) {
        format = @"%d nights";
    }

    [self.numberOfNights setText:[NSString stringWithFormat:format, self.selectedRate.hotel.numberOfNights]];
}

- (void)updateDateRange
{
    NSString *format = @"E, MMM d";
    NSString *checkInDateString = [CTEDateUtility convertDateToString:self.selectedRate.hotel.checkInDate withOutputFormat:format timeZone:[NSTimeZone defaultTimeZone]];
    NSString *checkOutDateString = [CTEDateUtility convertDateToString:self.selectedRate.hotel.checkOutDate withOutputFormat:format timeZone:[NSTimeZone defaultTimeZone]];
    [self.dateRange setText:[NSString stringWithFormat:@"%@ - %@", checkInDateString, checkOutDateString]];
}

- (void)loadImageFromURL:(NSString *)url
{
    NSURL *imageURL = [NSURL URLWithString:url];
    NSURLSessionDownloadTask *downloadTask = [[NSURLSession sharedSession] downloadTaskWithURL:imageURL completionHandler:^(NSURL *location, NSURLResponse *response, NSError *error) {
        UIImage *downloadedImage = [UIImage imageWithData:[NSData dataWithContentsOfURL:location]];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.image setImage:downloadedImage];

            // workaround for image scale being odd
            CGRect frame = self.image.frame;
            frame.size.height = 100;
            self.image.frame = frame;
        });
    }];
    [downloadTask resume];
}

- (void)fetchCreditCardsAndCancellationPolicy
{
    [self.creditCardButton setText:@"Loading Credit Cards..."];
    [self.cancellationPolicyButton setText:@"Loading Cancellation Policy..."];
    [self.selectedRate presellOptionsWithCompletionBlock:^(NSArray *creditCards, NSString *cancellationPolicy) {
        _creditCards = creditCards;
        _cancellationPolicy = cancellationPolicy;
        
        _selectedCreditCard = [self getDefaultCreditCard:creditCards];
        [self.creditCardButton setText:self.selectedCreditCard.name];
        [self.cancellationPolicyButton setText:@"Cancellation Policy & Rate Details"];
    }];
}

- (CTECreditCard *)getDefaultCreditCard:(NSArray *)creditCards
{
    for (int i=0; i<creditCards.count; i++) {
        CTECreditCard *tmp = (CTECreditCard *)[creditCards objectAtIndex:i];
        if ([tmp isDefault]) {
            return tmp;
        }
    }
    
    ALog(@"There is no default credit card");
    return nil;
}

- (IBAction)openCancellationPolicy
{
    if (self.cancellationPolicy && self.nextViewControllerBlock) {
        HotelCancellationPolicyViewController *viewController = [[HotelCancellationPolicyViewController alloc] initWithCancellationPolicy:self.cancellationPolicy];       
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Cancellation Policy" eventLabel:nil eventValue:nil];        
        self.nextViewControllerBlock(viewController);
    }
}

- (IBAction)openCreditCardSelection
{
    if (self.creditCards && self.nextViewControllerBlock) {
        CreditCardListTableViewController *viewController = [[CreditCardListTableViewController alloc] initWithCreditCards:self.creditCards selectedCard:self.selectedCreditCard completion:^(CTECreditCard *selectedCreditCard) {
            _selectedCreditCard = selectedCreditCard;
            [self.creditCardButton setText:self.selectedCreditCard.name];
        }];       
        [AnalyticsTracker logEventWithCategory:@"Hotel Booking" eventAction:@"Change Cards" eventLabel:nil eventValue:nil];        
        self.nextViewControllerBlock(viewController);
    }
}

// Need to expose a reserve method for the floating slide to reserve button
// It just serves as user confirmation, the data is all within this class
- (void)reserve
{
    if (!self.selectedCreditCard) {
        [self showAlertWithText:@"Missing credit card!"];
        return;
    }

    [WaitViewController showWithText:@"Reserving Room" animated:YES];

    NSArray *violationReasons = [self getViolationReasonsFromViolationViews];
    [self.selectedRate reserveWithCreditCard:self.selectedCreditCard violationReasons:violationReasons success:^(CTEHotelReserveConfirmation *reservation) {
        [WaitViewController hideAnimated:YES withCompletionBlock:^{
            [self goToItineraryWithRecordLocator:reservation.recordLocator itineraryLocator:reservation.itineraryLocator];
        }];
    } failure:^(CTEError *error) {
        [WaitViewController hideAnimated:YES withCompletionBlock:^{
            ALog(@"Failed to reserve room. %@", error);
            [self showAlertWithText:@"Failed to reserve room!"];
        }];
    }];
}

// This is a hack!
// Uses the legacy ConcurMobile itinerary loading code
- (void)goToItineraryWithRecordLocator:(NSString *)recordLocator itineraryLocator:(NSString *)itineraryLocator
{
    [WaitViewController showWithText:@"Loading Itinerary" animated:YES];
    _itineraryTransitionHack = [[HotelItineraryTransitionHack alloc] init];
    [self.itineraryTransitionHack requestHotelItineraryWithRecordLocator:recordLocator itineraryLocator:itineraryLocator completion:^{
        [WaitViewController hideAnimated:YES withCompletionBlock:^{ }];
    }];
}

// TODO: check if we need to enforce some business rules around violation reasons
- (NSArray *)getViolationReasonsFromViolationViews
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];
    for (HotelRoomViolationView *violationView in self.violationViews) {
        CTEHotelViolationReason *reason = [violationView violationReason];
        if (reason) {
            [tmp addObject:reason];
        }
    }

    return tmp;
}

- (void)showAlertWithText:(NSString *)text
{
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"" message:text delegate:nil cancelButtonTitle:@"close" otherButtonTitles:nil];
    [alert show];
}

@end
