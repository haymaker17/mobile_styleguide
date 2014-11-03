//
//  TrainFareChoicesVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "RailChoiceData.h"
#import "TrainGroupedCell.h"

@interface TrainFareChoicesVC : MobileViewController <UITableViewDelegate, UITableViewDataSource> {
	UILabel		*lblTrain1, *lblTrain1Time, *lblTrain1FromCity, *lblTrain1FromTime, *lblTrain1ToCity, *lblTrain1ToTime;
	UILabel		*lblTrain2, *lblTrain2Time, *lblTrain2FromCity, *lblTrain2FromTime, *lblTrain2ToCity, *lblTrain2ToTime;
	UILabel		*lblFromLabel, *lblFrom, *lblToLabel, *lblTo, *lblDateRange;
	UITableView	*tableList;
	NSMutableArray	*aKeys;
	NSMutableDictionary *dictGroups;
	RailChoiceData	*railChoice;

}

@property (strong, nonatomic) IBOutlet UILabel		*lblFromLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblFrom;
@property (strong, nonatomic) IBOutlet UILabel		*lblToLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblTo;
@property (strong, nonatomic) IBOutlet UILabel		*lblDateRange;
@property (strong, nonatomic) IBOutlet UITableView	*tableList;
@property (strong, nonatomic) NSMutableArray		*aKeys;
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

@property (strong, nonatomic) NSMutableArray        *taFields;  // TravelAuth fields for GOV

-(void) adjustLabel:(UILabel *) lblHeading LabelValue:(UILabel*) lblVal HeadingText:(NSString *) headText ValueText:(NSString *) valText ValueColor:(UIColor *) color W:(float)wOverride;
+(NSString *) fetchSegmentSeats:(NSString *)seatDescription NumberOfSeatsDesired:(int) numberOfSeatsDesired FrontToBack:(BOOL) isForward JustTheOne:(int) segmentPosition;

-(void) configureHeadingCell:(TrainGroupedCell*)cell atIndexPath:(NSIndexPath *)indexPath;
@end
