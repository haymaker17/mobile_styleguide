//
//  GoGoOfferViewController.h
//  ConcurMobile
//
//  Created by Richard Puckett on 11/25/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractGoGoViewController.h"

@interface GoGoOfferViewController : AbstractGoGoViewController

@property (strong, nonatomic) NSString *cardId;
@property (strong, nonatomic) NSString *itineraryId;
@property (strong, nonatomic) NSString *segmentKey;
@property (strong, nonatomic) UIView *waitView;
@property (strong, nonatomic) UIActivityIndicatorView *activityIndicator;
@property (strong, nonatomic) UILabel *waitLabel;

@property (weak, nonatomic) IBOutlet UILabel *card;

- (IBAction)didSelectCreditCard:(id)sender;
- (IBAction)didSelectPurchaseButton:(id)sender;

@end
