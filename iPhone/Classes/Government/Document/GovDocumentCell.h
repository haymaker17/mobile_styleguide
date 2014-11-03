//
//  GovDocumentCell.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/19/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface GovDocumentCell : UITableViewCell
{
    UILabel			*lblName, *lblAmount, *lblLine1, *lblLine2;
    UILabel         *lblRLine1;
    UIImageView     *img1, *img2;
}


@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UILabel *lblAmount;
@property (nonatomic, strong) IBOutlet UILabel *lblLine1;
@property (nonatomic, strong) IBOutlet UILabel *lblLine2;
@property (nonatomic, strong) IBOutlet UILabel *lblRLine1;
@property (strong, nonatomic) IBOutlet UIImageView *img1;
@property (strong, nonatomic) IBOutlet UIImageView *img2;

@end
