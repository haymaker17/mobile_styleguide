//
//  BookingChainCell.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/16/10.
//  Copyright 2010 Concur. All rights reserved.
//

@class MobileViewController;

@interface BookingChainCell : UITableViewCell
{
	UILabel			*chainNameLabel;
	UIImageView		*logoImageView;
}

@property (nonatomic, strong) IBOutlet UILabel			*chainNameLabel;
@property (nonatomic, strong) IBOutlet UIImageView		*logoImageView;

+(BookingChainCell*)makeCell:(UITableView*)tableView vc:(MobileViewController*)vc chainName:(NSString*)chainName logoImageUri:(NSString*)logoImageUri;

@end
