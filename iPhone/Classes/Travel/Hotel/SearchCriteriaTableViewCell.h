//
//  SearchCriteriaTableViewCell.h
//  PastDestinations
//
//  Created by Pavan Adavi on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "SearchCriteriaCellData.h"

@interface SearchCriteriaTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *lblTitle;
@property (weak, nonatomic) IBOutlet UILabel *lblSubTitle;
@property (weak, nonatomic) IBOutlet UIImageView *ivIcon;

-(void)setCellData:(SearchCriteriaCellData *)cellData;

@end
