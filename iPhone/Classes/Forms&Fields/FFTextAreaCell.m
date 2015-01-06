//
//  FFTextAreaCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 06/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"
#import "FFTextareaExternalEditorVC.h"




@implementation FFTextAreaCell

NSString *const FFCellReuseIdentifierTextArea = @"TextAreaCell";


#pragma mark - init

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{

    
    if (self = [super initWithStyle:style
                    reuseIdentifier:reuseIdentifier]){
        
    }
    return self;
}


#pragma mark - layout

-(void)initLayout{
    
    [super initLayout];
    
    [self.constraintsMetrics setObject:@3 forKey:@"spaceSeparator"];
    
    [self removeCellVisualFormatConstraints:@[@"VValue", @"VLineSep"]];
    [self addCellVisualFormatConstraints:@{
                                           @"VValue": @"V:[label(labelHeight)]-spaceSeparator-[labelValue]",
                                           @"VLineSep": @"V:[lineSeparator(lineSeparatorHeight)]|"
                                           }];
}


//public
-(CGFloat)heightView{
    
    CGFloat labelValuePositionY = 31.0;
    CGFloat margeBottom = 10.0;
    
    CGFloat heightView = labelValuePositionY + self.labelValue.bounds.size.height + margeBottom;
    return heightView + [self heightViewAddHeightOfLineSeparator];
}


-(void)markRW:(BOOL)isRW{
    
    [super markRW:isRW];
    
    [self setAccessoryType: isRW ? UITableViewCellAccessoryDisclosureIndicator : UITableViewCellAccessoryNone];
}


#pragma mark - component

-(UILabel*)createLabelValue{
    
    UILabel *label = [super createLabelValue];
        
    [label setLineBreakMode:NSLineBreakByWordWrapping];
    [label setNumberOfLines:0];
    
    return label;
}


//public
-(void)updateDataType{
    
    [super updateDataType];
    
    [self.labelValue setText:[self.field.dataType stringValue]];
    
    /* fix an issue : label.frame.size.width = ??? */
    double margeRight = [[self.constraintsMetrics objectForKey:@"margeRight"] doubleValue];
    if (self.accessoryType == UITableViewCellAccessoryDisclosureIndicator){
        
        margeRight = 40.0;
    }
    
    double labelValueWidth = CGRectGetWidth(self.contentView.frame) - ([[self.constraintsMetrics objectForKey:@"margeLeft"] doubleValue] + margeRight);
    
    CGRect frame = CGRectInfinite;
    frame.size.width = labelValueWidth;
    [self.labelValue setFrame:frame];
    /* end fix*/
    
    [self.labelValue sizeToFit];
}


#pragma mark - editor

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    
    [super setSelected:selected animated:animated];
    
    if (selected){
        
        FFTextareaExternalEditorVC *externalEditorVC = [[FFTextareaExternalEditorVC alloc] init];
        
        externalEditorVC.field = self.field;
        
        [self.delegateFC pushExternalEditorVC:externalEditorVC fromIndexPath:self.indexPath];
    }
}

@end
