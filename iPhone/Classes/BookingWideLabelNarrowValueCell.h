//
//  BookingWideLabelNarrowValueCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/28/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface BookingWideLabelNarrowValueCell : UITableViewCell
{
	UILabel		*labelLabel;
	UILabel		*labelValue;
}

@property (nonatomic, strong) IBOutlet UILabel	*labelLabel;
@property (nonatomic, strong) IBOutlet UILabel	*labelValue;

+(BookingWideLabelNarrowValueCell*)makeCell:(UITableView*)tableView owner:(id)owner label:(NSString*)labelText value:(NSString*)valueText;
+(BookingWideLabelNarrowValueCell*)makeEmptyCell:(UITableView*)tableView owner:(id)owner;

@end
