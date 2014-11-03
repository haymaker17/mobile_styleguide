//
//  QEEntryCell.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/6/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface QEEntryCell : UITableViewCell {
    UILabel     *lblHeading, *lblAmount, *lblSub1, *lblSub2;
    UIImageView *ivIcon1, *ivIcon2, *ivSelected;
}

@property (weak) UITableView *relatedTableView;

@property (strong, nonatomic) IBOutlet UILabel     *lblHeading;
@property (strong, nonatomic) IBOutlet UILabel     *lblAmount;
@property (strong, nonatomic) IBOutlet UILabel     *lblSub1;
@property (strong, nonatomic) IBOutlet UILabel     *lblSub2;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon1;
@property (strong, nonatomic) IBOutlet UIImageView *ivIcon2;
@property (strong, nonatomic) IBOutlet UIImageView *ivSelected;

-(void) updateAppearanceWithSelection:(BOOL)selected editing:(BOOL)editing deleting:(BOOL)deleting;

@end
