//
//  EntryCellBig.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/12/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
@class EntryListViewController;

@interface EntryCellBig : UITableViewCell {
	UILabel *label;
	UILabel *labelAmount;
	UILabel *labelStatus;
	UILabel *labelStatusTwo;
	UIButton *btnDetail;
	EntryListViewController *rootVC;
	NSUInteger *currentRow;
	UIImageView *image1;
	UIImageView *image2;
	UIImageView *image3;
	UIImageView *image4;
	
}

@property (nonatomic, retain) IBOutlet UILabel *label;
@property (nonatomic, retain) IBOutlet UILabel *labelAmount;
@property (nonatomic, retain) IBOutlet UILabel *labelStatus;
@property (nonatomic, retain) IBOutlet UILabel *labelStatusTwo;
@property (nonatomic, retain) IBOutlet UIButton *btnDetail;
@property (nonatomic, retain) EntryListViewController *rootVC;
@property (nonatomic) NSUInteger *currentRow;
@property (nonatomic, retain) IBOutlet UIImageView *image1;
@property (nonatomic, retain) IBOutlet UIImageView *image2;
@property (nonatomic, retain) IBOutlet UIImageView *image3;
@property (nonatomic, retain) IBOutlet UIImageView *image4;

- (IBAction)buttonDetailPressed:(id)sender;

@end
