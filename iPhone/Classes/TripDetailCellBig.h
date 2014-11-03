//
//  TripDetailCellBig.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/16/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class TripDetailsViewController;
@class RootViewController;


@interface TripDetailCellBig : UITableViewCell  <UITableViewDelegate, UITableViewDataSource>{
	UILabel				*label;
	UILabel				*labelDateRange;
	UILabel				*labelLocator;
	UIButton			*btnDetail;
	UIButton			*btnDrill;
	TripDetailsViewController *baseVC;
	RootViewController	*rootVC;
	NSUInteger			currentRow;
	
	UITableView			*tableList;
    NSMutableDictionary *tripBits;
    NSMutableArray		*keys;
	NSString			*tripKey;
	
}

@property (nonatomic, retain) IBOutlet UILabel		*label;
@property (nonatomic, retain) IBOutlet UILabel		*labelDateRange;
@property (nonatomic, retain) IBOutlet UILabel		*labelLocator;
@property (nonatomic, retain) IBOutlet UIButton		*btnDetail;
@property (nonatomic, retain) IBOutlet UIButton		*btnDrill;
@property (nonatomic, retain) TripDetailsViewController  *baseVC;
@property (nonatomic, retain) RootViewController	*rootVC;
@property (nonatomic) NSUInteger					currentRow;
@property (nonatomic, retain) NSString				*tripKey;

@property (nonatomic, retain) IBOutlet UITableView	*tableList;

@property (nonatomic, retain) NSMutableDictionary	*tripBits;
@property (nonatomic, retain) NSMutableArray		*keys;

- (IBAction)buttonDetailPressed:(id)sender;
- (IBAction)buttonDrillPressed:(id)sender IdKey:(NSString *)idKey;
-(void)resetTripBits:(NSMutableDictionary *)newBits KeyBits:(NSMutableArray *)keyBits;
- (void)drillToSegment:(NSString *)idKey;

@end
