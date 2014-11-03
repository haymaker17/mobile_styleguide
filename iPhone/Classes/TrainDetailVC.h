//
//  TrainDetailVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "RailChoiceData.h"
#import "CreditCard.h"
#import "DeliveryData.h"
#import "TrainDeliveryData.h"
#import "PadPickerPopoverVC.h"
#import "AmtrakSellData.h"

@interface TrainDetailVC : MobileViewController <PadPickerPopoverDelegate, UIAlertViewDelegate>
{
	UILabel		*lblTrain1, *lblTrain1Time, *lblTrain1FromCity, *lblTrain1FromTime, *lblTrain1ToCity, *lblTrain1ToTime;
	UILabel		*lblTrain2, *lblTrain2Time, *lblTrain2FromCity, *lblTrain2FromTime, *lblTrain2ToCity, *lblTrain2ToTime;
	UILabel		*lblFromLabel, *lblFrom, *lblToLabel, *lblTo, *lblDateRange;
	UITableView	*tableList;
	NSMutableArray	*aKeys, *aTrains, *aButtons;
	NSMutableDictionary *dictGroups;
	RailChoiceData	*railChoice;
	
	UILabel		*lblCost, *lblDelivery, *lblCard, *lblSeat1, *lblSeat2;
	UIButton	*btnDelivery, *btnCard;
	
	NSString	*deliveryOption;
	int			chosenCardIndex;
	CreditCard	*chosenCreditCard;
	DeliveryData	*deliveryData;
    
    NSArray				*violationReasons;
	NSArray				*violationReasonLabels;
	NSString			*violationReasonCode;
	NSString			*violationJustification;
	
	NSString		*from, *to, *dateRange;
	
	TrainDeliveryData *trainDeliveryData;
	
	UIActivityIndicatorView	*activity;
	UILabel					*lblLoading;
    
	PadPickerPopoverVC		*pickerPopOverVC;
	BOOL					isDelayingFirstCard;

    AmtrakSellData			*trainRezResponse;

}

@property (nonatomic, strong) PadPickerPopoverVC		*pickerPopOverVC;

@property (nonatomic,strong) IBOutlet UIActivityIndicatorView	*activity;
@property (nonatomic,strong) IBOutlet UILabel					*lblLoading;

@property (strong, nonatomic) TrainDeliveryData *trainDeliveryData;

@property (strong, nonatomic) NSString		*from;
@property (strong, nonatomic) NSString		*to;
@property (strong, nonatomic) NSString		*dateRange;

@property (strong, nonatomic) IBOutlet UILabel		*lblFromLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblFrom;
@property (strong, nonatomic) IBOutlet UILabel		*lblToLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblTo;
@property (strong, nonatomic) IBOutlet UILabel		*lblDateRange;
@property (strong, nonatomic) IBOutlet UITableView	*tableList;
@property (strong, nonatomic) NSMutableArray		*aKeys;
@property (strong, nonatomic) NSMutableArray		*aTrains;
@property (strong, nonatomic) NSMutableArray		*aButtons;
@property (strong, nonatomic) NSMutableDictionary	*dictGroups;

@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1Time;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1FromCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1FromTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1ToCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain1ToTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2Time;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2FromCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2FromTime;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2ToCity;
@property(strong, nonatomic) IBOutlet UILabel		*lblTrain2ToTime;

@property(strong, nonatomic) RailChoiceData			*railChoice;

@property(strong, nonatomic) IBOutlet UILabel		*lblSeat1;
@property(strong, nonatomic) IBOutlet UILabel		*lblSeat2;
@property(strong, nonatomic) IBOutlet UILabel		*lblCost;
@property(strong, nonatomic) IBOutlet UILabel		*lblDelivery;
@property(strong, nonatomic) IBOutlet UILabel		*lblCard;
@property(strong, nonatomic) IBOutlet UIButton		*btnDelivery;
@property(strong, nonatomic) IBOutlet UIButton		*btnCard;
@property(strong, nonatomic) DeliveryData			*deliveryData;

@property(strong, nonatomic) NSString				*deliveryOption;
@property int			chosenCardIndex;
@property(strong, nonatomic) CreditCard				*chosenCreditCard;
@property (strong, nonatomic) NSArray               *creditCards;

@property (strong, nonatomic) NSMutableArray        *taFields;  // TravelAuth fields for GOV

@property (nonatomic, strong) NSArray				*violationReasons;
@property (nonatomic, strong) NSArray				*violationReasonLabels;
@property (nonatomic, strong) NSString				*violationReasonCode;
@property (nonatomic, strong) NSString				*violationJustification;

-(void) showDelivery:(id)sender;
-(void) showCards:(id)sender;
-(void)chooseCard:(int)cardIndex;
-(void)chooseFirstCard;
-(void) makeReserveButton:(id)sender;
-(void) reserveTrain:(id)sender;
-(void) refreshOrientation;

-(void)fetchDeliveryOptions:(id)sender;
-(void)showHideWait:(BOOL)isShow;

- (void)pickerTapped:(id)sender IndexPath:(NSIndexPath *)indexPath;
@end
