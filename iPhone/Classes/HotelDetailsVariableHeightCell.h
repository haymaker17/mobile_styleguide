//
//  HotelDetailsVariableHeightCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface HotelDetailsVariableHeightCell : UITableViewCell
{
	UILabel		*nameLabel;
	UILabel		*descriptionLabel;
}

@property (nonatomic, strong) IBOutlet UILabel	*nameLabel;
@property (nonatomic, strong) IBOutlet UILabel	*descriptionLabel;

+(CGFloat)calculateCellHeight:(UITableView*)tableView hideCellLabel:(BOOL)hideCellLabel cellValue:(NSString*)val allowDisclosure:(BOOL)allowDisclosure;
+(HotelDetailsVariableHeightCell*)makeCell:(UITableView*)tableView owner:owner cellLabel:(NSString*)label cellValue:(NSString*)val allowDisclosure:(BOOL)allowDisclosure;

@end
