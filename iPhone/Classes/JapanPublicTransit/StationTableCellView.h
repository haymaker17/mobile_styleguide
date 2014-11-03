//
//  StationTableCellView.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/27/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface StationTableCellView : UIView

@property (weak, nonatomic) IBOutlet UIView *view;
@property (weak, nonatomic) IBOutlet UILabel *stationName;
@property (weak, nonatomic) IBOutlet UILabel *lineName;
@property (weak, nonatomic) IBOutlet UILabel *price;

- (id)initWithNib:(NSString *)nibName;

@end
