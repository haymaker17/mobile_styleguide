//
//  GoGoOfferViewController.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"
#import "BuyGoGoPassRequestFactory.h"
#import "CreditCardManager.h"
#import "CreditCardPickerViewController.h"
#import "CXClient.h"
#import "CXRequest.h"
#import "GoGoMessageRequestFactory.h"
#import "GoGoOfferViewController.h"
#import "GoGoPurchaseConfirmationViewController.h"
#import "SearchYodleeCardsVC.h"
#import "TravelRequestFactory.h"
#import "WebViewController.h"

@interface GoGoOfferViewController ()

@end

@implementation GoGoOfferViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    if (self) {
        self.title = @"Concur Offer";
    }
    
    return self;
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [[AnalyticsManager sharedInstance] logCategory:@"Offer" withName:@"Viewed"];
    
    // Wow, if these creds aren't set then *app* crashes.
    //
    [Parse setApplicationId:@"nNBmg91Dgq5ByD6xxStwSjw0Sra1mphCcUXXS0tL"
                  clientKey:@"dMlRnik84r4SWt7LCvbI6v55JEiCmZPxS81zagbD"];
    
    PFQuery *query = [PFQuery queryWithClassName:@"GoGoMessage"];
    
    [query whereKey:@"messageId" equalTo:self.message.messageId];
    
    [query getFirstObjectInBackgroundWithBlock:^(PFObject *object, NSError *error) {
        if (error != nil) {
            DLog(@"Error retrieving object for message %@: %@",
                 self.message.messageId, error.localizedDescription);
            return;
        }
        
        if (object[@"viewedAt"] == nil) {
            object[@"viewedAt"] = [NSDate date];
            [object save];
        }
    }];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    if ([CreditCardManager.sharedInstance.selectedCard isEqualToString:@""]) {
        [self fetchCreditCards];
    }
    
    self.waitView = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.waitView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.8];
    
    self.activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    self.activityIndicator.center = self.waitView.center;
    [self.waitView addSubview:self.activityIndicator];
    
    self.waitLabel = [[UILabel alloc] initWithFrame:[UIScreen mainScreen].bounds];
    self.waitLabel.text = @"Purchasing Pass";
    self.waitLabel.textAlignment = NSTextAlignmentCenter;
    self.waitLabel.textColor = [UIColor lightGrayColor];
    CGPoint labelCenter = self.activityIndicator.center;
    labelCenter.y = self.activityIndicator.frame.origin.y + self.activityIndicator.frame.size.height + 20;
    self.waitLabel.center = labelCenter;
    [self.waitView addSubview:self.waitLabel];
    
    [self.activityIndicator startAnimating];
    [self.navigationController.view addSubview:self.waitView];
    
    self.waitView.alpha = 0;
    
    // Clear out the app icon badge now. Since this is the only IPM offer right now
    // we clear the whole thing.
    //
    //if ([[UIApplication sharedApplication] applicationIconBadgeNumber] > 0) {
    //    [[UIApplication sharedApplication] setApplicationIconBadgeNumber:0];
    //}
    
    // As we move toward more generalized IPM, we now just decrement badge count.
    //
    int badgeCount = [[UIApplication sharedApplication] applicationIconBadgeNumber];
    
    if (badgeCount > 0) {
        int newBadgeCount = badgeCount - 1;
        
        [[UIApplication sharedApplication] setApplicationIconBadgeNumber:newBadgeCount];
    }
}

- (void)viewDidDisappear:(BOOL)animated {
    [super viewDidDisappear:animated];
    
    self.waitView.alpha = 0;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.card.text = CreditCardManager.sharedInstance.selectedCard;
}

#pragma mark - Responders

- (IBAction)didSelectCreditCard:(id)sender {
    CreditCardPickerViewController *nextController = [[CreditCardPickerViewController alloc] init];
    
    self.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:[Localizer getLocalizedText:@"Back"]
                                                                             style:UIBarButtonItemStylePlain
                                                                            target:nil
                                                                            action:nil];
    
    [self.navigationController pushViewController:nextController animated:YES];
}

- (IBAction)didSelectPurchaseButton:(id)sender {
    if (![ExSystem connectedToNetwork]) {
        [self showOfflineAlert];
        return;
    }
    
    [UIView animateWithDuration:0.25 animations:^{
        self.waitView.alpha = 1;
    }];
    
    // Stashing some code to allow for blurred background filtering in future.
    //
    //    UIWindow *w = [[UIApplication sharedApplication] keyWindow];
    //
    //    if ([[UIScreen mainScreen] respondsToSelector:@selector(scale)]) {
    //        UIGraphicsBeginImageContextWithOptions(w.bounds.size, NO, [UIScreen mainScreen].scale);
    //    } else {
    //        UIGraphicsBeginImageContext(w.bounds.size);
    //    }
    //
    //    [w.layer renderInContext:UIGraphicsGetCurrentContext()];
    //    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    //    UIGraphicsEndImageContext();
    //
    //    NSData *data = UIImagePNGRepresentation(image);
    
    [GoGoMessageRequestFactory messageRequestForMessageId:self.message.messageId
                                             successBlock:^(GoGoOffer *offer) {
                                                 self.itineraryId = offer.itineraryId;
                                                 self.segmentKey = offer.segmentKey;
                                                 [self beginTransaction];
                                             } failureBlock:^(NSError *error) {
                                                 NSLog(@"Error = %@", error);
                                                 [self showError];
                                             }];
}

- (void)beginTransaction {
    /*
     NSString *mockResponse =
     @"<Response xmlns:xsi=""http://www.w3.org/2001/XMLSchema-instance"">"
     @"<ConfirmationCode>GC371587</ConfirmationCode>"
     @"</Response>";
     */
    
    SimpleCreditCard *c = [[CreditCardManager sharedInstance] selectedSimpleCard];
    
    CXRequest *request = [BuyGoGoPassRequestFactory buyGoGoPassWithItineraryLocator:self.itineraryId
                                                                      andSegmentKey:self.segmentKey
                                                                    andCreditCardId:c.cardId];
    
    [[CXClient sharedClient] performRequest:request
                                    success:^(NSString *result) {
                                        NSString *confirmationCode = [self extractConfirmationCodeFromString:result];
                                        
                                        if (confirmationCode == nil) {
                                            [self showError];
                                        } else {
                                            [self replaceOfferMessageWithConfirmationCode:confirmationCode];
                                            [self showSuccessScreenWithConfirmationCode:confirmationCode];
                                        }
                                    } failure:^(NSError *error) {
                                        [self showError];
                                    }];
}

- (void)replaceOfferMessageWithConfirmationCode:(NSString *)confirmationCode {
    MessageCenterManager *messageCenterManager = [MessageCenterManager sharedInstance];
    
    [messageCenterManager removeMessageAtIndex:0];
    
    MessageCenterMessage *message = [[MessageCenterMessage alloc] init];
    message.iconName = @"gogo.png";
    message.title = @"Gogo Wifi Confirmation Code";
    message.message = confirmationCode;
    message.stringExtra = confirmationCode;
    message.commandName = @"GoGoPurchaseConfirmationViewController";
    
    [messageCenterManager addMessage:message silently:YES];
}

- (NSString *)extractConfirmationCodeFromString:(NSString *)str {
    NSString *confirmationCode = nil;
    
    RXMLElement *xml = [RXMLElement elementFromXMLString:str encoding:NSUTF8StringEncoding];
    
    if (xml != nil) {
        confirmationCode = [xml child:@"ConfirmationCode"].text;
    }
    
    return confirmationCode;
}

- (void)fetchCreditCards {
    CXRequest *request = [TravelRequestFactory creditCardsForLoginId:@""
                                                         andTravelId:1
                                                        forCardTypes:CardTypePersonal];
    
    [[CXClient sharedClient] performRequest:request success:^(NSString *result) {
        RXMLElement *rootXML = [RXMLElement elementFromXMLString:result encoding:NSUTF8StringEncoding];
        [[CreditCardManager sharedInstance] loadFromXml:rootXML];
        self.card.text = CreditCardManager.sharedInstance.selectedCard;
    } failure:^(NSError *error) {
        NSLog(@"Error = %@", error);
    }];
}

// Throw this into the background so it doesn't block networking.
//
- (void)showError {
    [UIView animateWithDuration:0.25 animations:^{
        self.waitView.alpha = 0;
    }];
    
    NSBlockOperation *op = [NSBlockOperation blockOperationWithBlock:^{
        [[AnalyticsManager sharedInstance] logCategory:@"Offer" withName:@"Purchase Failed"];
        
        [self.activityIndicator stopAnimating];
        [self.waitView removeFromSuperview];
        
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Purchase Failed!"
                                                        message:@"Sorry! Something went wrong when we tried to send the info to Gogo. Please try again later."
                                                       delegate:nil
                                              cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                              otherButtonTitles:nil];
        
        [alert show];
    }];
    
    [[NSOperationQueue mainQueue] addOperation:op];
}

- (void)showOfflineAlert {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Offline"
                                                    message:@"Purchases cannot be made while your device is offline. Please reconnect to the Internet and try again."
                                                   delegate:nil
                                          cancelButtonTitle:[Localizer getLocalizedText:@"OK"]
                                          otherButtonTitles:nil];
    
    [alert show];
}

- (void)showSuccessScreenWithConfirmationCode:(NSString *)confirmationCode {
    [[AnalyticsManager sharedInstance] logCategory:@"Offer" withName:@"Purchase Succeeded"];
    
    [self.activityIndicator stopAnimating];
    [self.waitView removeFromSuperview];
    
    GoGoPurchaseConfirmationViewController *nextController = [[GoGoPurchaseConfirmationViewController alloc] init];
    
    //nextController.confirmationCodeString = confirmationCode;
    
    [self.navigationController pushViewController:nextController animated:YES];
}

@end
