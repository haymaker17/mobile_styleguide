//
//  ListItemCell.h
//  ConcurMobile
//
//  Created by yiwen on 4/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ListItemCell : UITableViewCell 
{
    UILabel			*lblName, *lblGroup;
}

@property (nonatomic, strong) IBOutlet UILabel *lblName;
@property (nonatomic, strong) IBOutlet UILabel *lblGroup;

-(void) resetCellContent:(NSString*)name withGroup:(NSString*)group; 

@end
