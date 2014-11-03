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

@interface MessageCenterViewController ()

@end

@implementation MessageCenterViewController

- (id)init {
    self = [super init];
    
    if (self) {
        self.messageCenterManager = [MessageCenterManager sharedInstance];
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

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // If iOS 7
    //
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
    
    /*
     if ([self isDevelopmentBuild]) {
     UIBarButtonItem *addMockOffer = [[UIBarButtonItem alloc] initWithBarButtonSystemItem:UIBarButtonSystemItemAdd
     target:self
     action:@selector(addMockOffer:)];
     
     self.navigationItem.rightBarButtonItem = addMockOffer;
     }
     */
    
    // call IPM to get the details we need to pass to DfP
    IpmRequest *request = [[IpmRequest alloc] initWithTarget:@"mobileMessageCenter"];
    [request requestIpmMessagesWithSuccess:^(NSArray *messages) {
        if (messages != nil && [messages count] > 0){
            IpmMessage *firstMessage = messages[0];
            
            // Setup DfP
            self.dfpBannerView = [[DFPBannerView alloc] initWithAdSize:kGADAdSizeSmartBannerPortrait];
            [self.dfpBannerView setAdUnitID:firstMessage.adUnitId];
            [self.dfpBannerView setRootViewController:self];
            
            // Add DfP banner to view
            [self.view addSubview:self.dfpBannerView];
            
            // Pass additional parameters to DfP for banner customisation
            GADRequest *request = [GADRequest request];
            DFPExtras *extras = [[DFPExtras alloc] init];
            [extras setAdditionalParameters:firstMessage.additionalParameters];
            [request registerAdNetworkExtras:extras];
            
            // Tell the banner to fetch content from DfP
            [self.dfpBannerView loadRequest:request];
        }
    } failure:^(CTEError *error) {
        ALog(@"Whoops, couldn't get IPM for MessageCenter: %@", [error localizedDescription]);
    }];
    
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    [self.table reloadData];
}

- (void)closeMe:(id)sender {
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

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        int row = [indexPath row];
        
        [self.messageCenterManager removeMessageAtIndex:row];
        
        [tableView beginUpdates];
        
        [tableView deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
        
        [tableView endUpdates];
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self.messageCenterManager numMessagesForType:MessageTypeAny];
}

#pragma mark - UITableViewDelegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:YES];
    
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

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    MessageCenterMessage *message = [self.messageCenterManager messageAtIndex:[indexPath row]];
    
    CGFloat titleHeight = [self heightForString:message.title withFont:[UIFont fontWithName:@"Helvetica Neue" size:15]];
    CGFloat messageHeight = [self heightForString:message.message withFont:[UIFont fontWithName:@"Helvetica Neue" size:13]];
    
    // Add labels, plus cell margin, plus inter-label padding.
    //
    CGFloat height = titleHeight + messageHeight + 40 + 5;
    
    return height;
}

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

/*
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
 */

@end
