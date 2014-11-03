//
//  LoadingSpinnerTableViewCell.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 8/14/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DistractorImageView.h"
#import "LoadingSpinnerCellData.h"

@interface LoadingSpinnerTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UILabel *lblCaption;
@property (weak, nonatomic) IBOutlet UIImageView *ivLoadingSpinner;
-(void)setCellData:(LoadingSpinnerCellData *)cellData;

@end
