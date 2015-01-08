//
//  MessageCenterViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/7/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "BuyGoGoPassRequestFactory.h"
#import "CreditCardManager.h"
#import "CXClient.h"
#import "CXRequest.h"
#import "GoGoOfferViewController.h"
#import "MessageCenterTableCell.h"
#import "MessageCenterViewController.h"
#import "RXMLElement.h"
#import "TravelRequestFactory.h"
#import "CTENetworkSettings.h"
#import "CTENetworking.h"
#import "CTEError.h"
#import "IpmRequest.h"
#import "IpmMessage.h"
#import "DFPExtras.h"
#import "GADAdMobExtras.h"
#import "HotelSearchTableViewController.h"
#import "HotelViewController.h"
#import "SendFeedBackVC.h"

@interface MessageCenterViewController ()
@property (strong, nonatomic) NSMutableArray *messageArray;
@property BOOL isAlpha;
@end

@implementation MessageCenterViewController

- (id)init {
    self = [super init];
    
    if (self) {
        self.messageCenterManager = [MessageCenterManager sharedInstance];
    }
    
    NSString *appVersion = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"];
    if ([appVersion rangeOfString:@"alpha"].location != NSNotFound) {
        self.isAlpha = YES;
    }
    
    
    return self;
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        // Empty.
    }
    
    return self;
}

// Based on: https://gist.github.com/steipete/7668246
//
- (BOOL)isDevelopmentBuild {
#if TARGET_IPHONE_SIMULATOR
    return YES;
#else
    static BOOL isDevelopment = NO;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        // There is no provisioning profile in AppStore Apps.
        NSData *data = [NSData dataWithContentsOfFile:[NSBundle.mainBundle pathForResource:@"embedded" ofType:@"mobileprovision"]];
        if (data) {
            const char *bytes = [data bytes];
            NSMutableString *profile = [[NSMutableString alloc] initWithCapacity:data.length];
            for (NSUInteger i = 0; i < data.length; i++) {
                [profile appendFormat:@"%c", bytes[i]];
            }
            // Look for debug value, if detected we're a development build.
            NSString *cleared = [[profile componentsSeparatedByCharactersInSet:NSCharacterSet.whitespaceAndNewlineCharacterSet] componentsJoinedByString:@""];
            isDevelopment = [cleared rangeOfString:@"<key>get-task-allow</key><true/>"].length > 0;
        }
    });
    return isDevelopment;
#endif
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // If iOS 7
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        self.edgesForExtendedLayout = UIRectEdgeNone;
    }
    
    if([UIDevice isPad])
    {
        UIBarButtonItem *btnClose = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Close"]
                                                                     style:UIBarButtonItemStyleBordered
                                                                    target:self
                                                                    action:@selector(closeMe:)];
        self.navigationItem.leftBarButtonItem = btnClose;
    }
    
    self.title = @"Message Center";
    
    if (self.isAlpha)
    {
        //     if ([self isDevelopmentBuild]) {
        //     UIBarButtonItem *addMockOffer = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
        //     target:self
        //     action:@selector(addMockOffer:)];
        //
        //     self.navigationItem.rightBarButtonItem = addMockOffer;
        //     }
        
        [self loadDFPbanner];
    }
}

- (void)loadDFPbanner
{
    // Huge apologies to all for the content below, this area is in constant development
    //
    //
    //
    //
    //
    //
    
    self.messageArray = [[NSMutableArray alloc] init];
    
    // call IPM to get the details we need to pass to DfP
    IpmRequest *ipmRequest = [[IpmRequest alloc] initWithTarget:@"mobileMessageCenter2"];
    [ipmRequest requestIpmMessagesWithSuccess:^(NSArray *messages) {
        if (messages != nil && [messages count] > 0){
            // Expand the messages...
//            NSMutableArray *newArray = [[NSMutableArray alloc] init];
//            [newArray addObjectsFromArray:messages];
//            [newArray addObjectsFromArray:messages];
//            [newArray addObjectsFromArray:messages];
//            [newArray addObjectsFromArray:messages];
//            messages = newArray;
            for (IpmMessage *singleMessage in messages)
            {
                DFPBannerView *newBanner;
                
                // Setup DfP
                //            GADAdSize customAdSize = GADAdSizeFromCGSize(CGSizeMake(808, 253));
                if ([UIDevice isPad])
                {
                    newBanner = [[DFPBannerView alloc] initWithAdSize:GADAdSizeFromCGSize(CGSizeMake(540, 297))];
                } else {
                    newBanner = [[DFPBannerView alloc] initWithAdSize:GADAdSizeFromCGSize(CGSizeMake(300, 165))];
                }
                //NSLog(@"pane width: %f", self.view.bounds.size.width);
                //NSLog(@"pane height: %f", self.view.bounds.size.height);
                //self.dfpBannerView = [[DFPBannerView alloc] initWithAdSize: kGADAdSizeSmartBannerPortrait];
                [newBanner setAdUnitID:singleMessage.adUnitId];
                //[self.dfpBannerView setAdUnitID:@"/19197427/Dev/DevMobile"];
                //[self.dfpBannerView setAdUnitID:@"/19197427/Dev/DevMobileImages"];
                //[self.dfpBannerView setAdUnitID:@"/19197427/Dev/DevMobileTextAd"];
                //[self.dfpBannerView setAdUnitID:@"/19197427/Dev/DevMobileAlpha"];
                NSLog(@"Ad - %@", singleMessage.adUnitId);
                [newBanner setRootViewController:self];
                [newBanner setAppEventDelegate:self];
                [newBanner setDelegate:self];
                //            [self.dfpBannerView setEnableManualImpressions:YES];
                
                // Add DfP banner to view
                //            [self.view addSubview:self.dfpBannerView];
                //NSLog(@"pane width: %f", self.view.bounds.size.width);
                
                // Pass additional parameters to DfP for banner customisation
                GADRequest *request = [GADRequest request];
                DFPExtras *extras = [[DFPExtras alloc] init];
                [extras setAdditionalParameters:singleMessage.additionalParameters];
                [request registerAdNetworkExtras:extras];
                
                // Tell the banner to fetch content from DfP
                [newBanner loadRequest:request];
                //            NSLog(@"%f", self.dfpBannerView.bounds.origin.x);
                //            NSLog(@"%f", self.dfpBannerView.bounds.origin.y);
                //            NSLog(@"%f", self.dfpBannerView.bounds.size.height);
                //            NSLog(@"%f", self.dfpBannerView.bounds.size.width);
                [self.messageArray addObject:newBanner];
            }
            [self.table reloadData];
        }
    } failure:^(CTEError *error) {
        ALog(@"Whoops, couldn't get IPM for MessageCenter: %@", [error localizedDescription]);
    }];
}

/// Called when an ad request loaded an ad.
- (void)adViewDidReceiveAd:(DFPBannerView *)adView {
    NSLog(@"adViewDidReceiveAd:%@", adView.adUnitID);
    //    [self.messageArray addObject:adView];
    //    [self.table reloadData];
}

/// Called when an ad request failed.
- (void)adView:(DFPBannerView *)adView didFailToReceiveAdWithError:(GADRequestError *)error {
    NSLog(@"adViewDidFailToReceiveAdWithError: %@ %@", adView.adUnitID, [error localizedDescription]);
}

- (void)adView:(DFPBannerView *)banner didReceiveAppEvent:(NSString *)name withInfo:(NSString *)info {
    // Huge apologies to all for the content below, this area is in constant development
    //
    //
    //
    //
    //
    //
    
    NSLog(@"app event");
    if ([name isEqualToString:@"goto"]) {
        NSLog(@"goto");
        if ([info isEqualToString:@"uber"]) {
            NSLog(@"call uber");
            // lets go visit Uber!
            UIViewController *webViewController = [[UIViewController alloc] init];
            
            UIWebView *uiWebView = [[UIWebView alloc] initWithFrame: CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height+self.view.frame.origin.y)];
            
            //            UIWebView *uiWebView = [[UIWebView alloc] init];
            //            uiWebView.frame = self.view.bounds;
            //            CGRect bounds = self.view.bounds;
            //            CGRect frame = self.view.frame;
            [uiWebView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"https://m.uber.com/sign-up?client_id=xxxx"]]];
            [uiWebView setScalesPageToFit:YES];
            [webViewController.view addSubview: uiWebView];
            
            [self.navigationController pushViewController:webViewController animated:YES];
            
        } else if ([info isEqualToString:@"hotel"]) {
            NSLog(@"book hotel");
            // lets book a hotel!
            [self openHotelBooking];
        } else if ([info isEqualToString:@"feedback"]) {
            NSLog(@"send feedback");
            // lets send alpha feedback!
            [self openFeedBacks:self];
        } else {
            // nothing
            NSLog(@"nothing");
        }
    }
}

/**
 *  Open a email dialog box to send feedbacks to "mobilealphafeedbackios@concur.com"
 */
- (void)openFeedBacks:(UIViewController *)parentView
{
    if (![MFMailComposeViewController canSendMail])
    {
        UIAlertView *alert = [[MobileAlertView alloc]
                              initWithTitle:[Localizer getLocalizedText:@"Mail Unavailable"]
                              message:[Localizer getLocalizedText:@"This device is not configured for sending mail."]
                              delegate:nil
                              cancelButtonTitle:[Localizer getLocalizedText:@"LABEL_CLOSE_BTN"]
                              otherButtonTitles:nil];
        [alert show];
    }
    else{
        SendFeedBackVC *vc = [[SendFeedBackVC alloc] init];
        [vc sendLogAction];
        if([UIDevice isPad]){
            vc.modalPresentationStyle = UIModalPresentationFormSheet;
            [parentView presentViewController:vc animated:YES completion:nil];
        }
        else{
            [parentView presentViewController:vc animated:YES completion:nil];
        }
    }
}
- (void)openHotelBooking
{
    if ([UIDevice isPad] && self.navigationController == nil)
    {
        if ([Config isNewHotelBooking] && [UIDevice isPhone]) {
            [HotelSearchTableViewController showHotelsNearMe:[self getBookTripsNavigationController]];
        }else{
            [HotelViewController showHotelVC:[self getBookTripsNavigationController] withTAFields:nil];
        }
    }else{
        if ([Config isNewHotelBooking] && [UIDevice isPhone]) {
            [HotelSearchTableViewController showHotelsNearMe:self.navigationController];
        }else{
            [HotelViewController showHotelVC:self.navigationController withTAFields:nil];
        }
    }
}

-(UINavigationController*)getBookTripsNavigationController
{
    TripsViewController *tripsListVC = [[TripsViewController alloc] initWithNibName:@"TripsView" bundle:nil];
    UINavigationController *navcontroller = [[UINavigationController alloc] initWithRootViewController:tripsListVC];
    navcontroller.modalPresentationStyle = UIModalPresentationFormSheet;
    
    [navcontroller setToolbarHidden:NO];
    
    return navcontroller;
}

- (void)dealloc {
    [self.dfpBannerView setAppEventDelegate:nil];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
}

- (void)closeMe:(id)sender {
    NSLog(@"%f", self.dfpBannerView.bounds.origin.x);
    NSLog(@"%f", self.dfpBannerView.bounds.origin.y);
    NSLog(@"%f", self.dfpBannerView.bounds.size.height);
    NSLog(@"%f", self.dfpBannerView.bounds.size.width);
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)copyConfirmationString:(NSString *)confirmationString {
    UIPasteboard *pb = [UIPasteboard generalPasteboard];
    
    [pb setString:confirmationString];
}

#pragma mark - Alert

- (void)showFailureAlert {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Purchase Did Not Go Through"
                                                    message:@"Sorry! Something went wrong when we tried to send the info to Gogo. Please try again later."
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    
    [alert show];
}

- (void)showOfflineAlert {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Offline"
                                                    message:@"Purchases cannot be made while your device is offline. Please reconnect to the Internet and try again."
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    
    [alert show];
}

#pragma mark - UIAlertView delegate

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex {
}

#pragma mark - UITableViewDataSource

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    if (self.isAlpha)
    {
        UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"tableCell"];
        
        if (cell == nil) {
            cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"tableCell"];
        }
        
        if (self.messageArray != nil && [self.messageArray count] > 0)
        {
            DFPBannerView *cellBanner = self.messageArray[(int)indexPath.row];
            [cell addSubview:cellBanner];
            CGFloat cellHeight = [self tableView:tableView heightForRowAtIndexPath:indexPath];
            cellBanner.center = CGPointMake(cell.contentView.bounds.size.width/2, cellHeight/2);
        }
        
        return cell;
    }
    else
    {
        MessageCenterTableCell *cell = [tableView
                                        dequeueReusableCellWithIdentifier:@"MessageViewTableCell"];
        
        if (cell == nil) {
            NSArray *nib = [[NSBundle mainBundle] loadNibNamed:@"MessageCenterTableCell" owner:self options:nil];
            cell = [nib objectAtIndex:0];
        }
        
        NSUInteger row = [indexPath row];
        
        MessageCenterMessage *message = [self.messageCenterManager messageAtIndex:row];
        
        cell.image.image = [UIImage imageNamed:message.iconName];
        
        cell.title.text = message.title;
        [cell.title sizeToFit];
        
        CGRect titleFrame = cell.title.frame;
        
        cell.message.text = message.message;
        [cell.message sizeToFit];
        
        CGRect messageFrame = CGRectMake(cell.message.frame.origin.x,
                                         titleFrame.origin.y + titleFrame.size.height + 5,
                                         cell.message.frame.size.width,
                                         cell.message.frame.size.height);
        
        cell.message.frame = messageFrame;
        
        if (message.commandName == nil) {
            cell.accessoryType = UITableViewCellAccessoryNone;
        }
        
        return cell;
    }
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        int row = (int)[indexPath row];
        
        [self.messageCenterManager removeMessageAtIndex:row];
        
        [tableView beginUpdates];
        
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [tableView endUpdates];
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    if (self.isAlpha)
    {
        if (self.messageArray != nil && [self.messageArray count] > 0)
        {
            return self.messageArray.count;
        }
    }
    else{
        return [self.messageCenterManager numMessagesForType:MessageTypeAny];
    }
    return 0;
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
    if (!self.isAlpha)
    {
        if (![ExSystem connectedToNetwork]) {
            [self showOfflineAlert];
            return;
        }
        
        NSUInteger row = [indexPath row];
        
        MessageCenterMessage *message = [self.messageCenterManager messageAtIndex:row];
        
        [self.messageCenterManager setType:MessageTypeRead forMessage:message];
        
        // Temporary hack specifically for single instance Gogo.
        //
        if (message.commandName == nil) {
            [self copyConfirmationString:message.stringExtra];
        } else {
            [self navigateToOffer:message];
        }
    }
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    CGFloat height = 0;
    
    if (self.isAlpha)
    {
        if ([UIDevice isPad])
        {
            height = 297;
        }
        else
        {
            height = 176;
        }
    }
    else
    {
        MessageCenterMessage *message = [self.messageCenterManager messageAtIndex:[indexPath row]];
        
        CGFloat titleHeight = [self heightForString:message.title withFont:[UIFont fontWithName:@"Helvetica Neue" size:15]];
        CGFloat messageHeight = [self heightForString:message.message withFont:[UIFont fontWithName:@"Helvetica Neue" size:13]];
        
        // Add labels, plus cell margin, plus inter-label padding.
        //
        height = titleHeight + messageHeight + 40 + 5;
    }
    
    return height;
}

//- (CGFloat)tableView:(UITableView *)tableView estimatedHeightForRowAtIndexPath:(NSIndexPath *)indexPath
//{
//
//    CGFloat height = 0;
//
//    if ([UIDevice isPad])
//    {
//        height = 297;
//    }
//    else
//    {
//        height = 176;
//    }
//
//    return height;
//}

#pragma mark - Util

- (CGFloat)heightForString:(NSString *)str withFont:(UIFont *)font {
    CGFloat width = 206;
    NSAttributedString *attributedText =
    [[NSAttributedString alloc]
     initWithString:str
     attributes:@
     {
     NSFontAttributeName: font
     }];
    
    CGRect rect = [attributedText boundingRectWithSize:(CGSize){width, CGFLOAT_MAX}
                                               options:NSStringDrawingUsesLineFragmentOrigin
                                               context:nil];
    CGSize size = rect.size;
    
    CGFloat height = ceilf(size.height);
    
    return height;
}

- (void)navigateToOffer:(MessageCenterMessage *)message {
    Class clazz = NSClassFromString(message.commandName);
    
    if (clazz != nil) {
        AbstractIpmViewController *vc = (AbstractIpmViewController *) [[clazz alloc] init];
        
        vc.message = message;
        
        [self.navigationController pushViewController:vc animated:YES];
        
        [[NSNotificationCenter defaultCenter] postNotificationName:@"concur_message_center_read_new" object:message];
    }
}


- (void)addMockOffer:(id)sender {
    MessageCenterManager *messageCenterManager = [MessageCenterManager sharedInstance];
    
    MessageCenterMessage *message = [[MessageCenterMessage alloc] init];
    
    message.iconName = @"gogo.png";
    message.title = @"Gogo Wifi is available on your upcoming flight.";
    message.message = @"All-Day Pass is $14.00 when you buy now through Concur.";
    message.commandName = @"GoGoOfferViewController";
    message.messageId = @"23";
    message.status = MessageStatusUnread;
    message.isSilent = NO;
    
    [messageCenterManager addMessage:message];
    
    UILocalNotification *notification = [[UILocalNotification alloc] init];
    notification.alertBody = @"All-Day Pass is $14.00 when you buy now through Concur.";
    notification.soundName = UILocalNotificationDefaultSoundName;
    
    [[UIApplication sharedApplication] presentLocalNotificationNow:notification];
    
    int badgeCount = [[UIApplication sharedApplication] applicationIconBadgeNumber];
    
    if (badgeCount > 0) {
        int newBadgeCount = badgeCount + 1;
        
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:newBadgeCount];
    }
    
    [self.table reloadData];
}


@end
