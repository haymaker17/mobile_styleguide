//
//  SearchHeaderViewCell.h
//  PastDestinations
//
//  Created by Pavan Adavi on 6/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchTableHeaderCellData.h"

@interface SearchHeaderViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *lblLocation;
@property (weak, nonatomic) IBOutlet UILabel *lblStayDates;
@property (weak, nonatomic) IBOutlet UIImageView *ivLocationIcon;

-(void)setCellData:(SearchTableHeaderCellData *)cellData;

@end
