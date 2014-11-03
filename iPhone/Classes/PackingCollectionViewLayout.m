//
//  PackingCollectionViewLayout.m
//  ConcurHomeCollectionView
//
//  Created by ernest cho on 11/21/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "PackingCollectionViewLayout.h"

@interface PackingCollectionViewLayout()
@property (nonatomic, readwrite, assign) CGFloat viewWidth;
@property (nonatomic, readwrite, assign) CGFloat spacerX;
@property (nonatomic, readwrite, assign) CGFloat spacerY;

// used to track all options on where to place the next cell
@property (nonatomic, readwrite, strong) NSMutableArray *candidatePositions;
@end

@implementation PackingCollectionViewLayout

- (id)init
{
    self = [super init];
    if (self)
    {
        [self sharedInit];
    }
    return self;
}

- (void)awakeFromNib
{
    [self sharedInit];
}

- (void)sharedInit
{
    self.spacerX = 8;
    self.spacerY = 8;
}

- (NSArray *)layoutAttributesForElementsInRect:(CGRect)rect
{
    NSArray *array = [super layoutAttributesForElementsInRect:rect];

    // I probably should have not subclassed the FlowLayout, but it was easier than watching the hour long video on how to build a full custom layout.
    [self recalculatePositions:array];
    return array;
}

/**
 Simplified box packing algorithm.
 
 This does NOT check for cell overlap.  This is because our current layout doesn't require it.
 */
- (void)recalculatePositions:(NSArray *)array
{
    // the amount of space I'm working with.
    self.viewWidth = self.collectionView.frame.size.width;
    self.candidatePositions = [[NSMutableArray alloc] init];

    // next place to put a cell
    CGPoint position = CGPointMake(self.spacerX, self.spacerY);

    for (UICollectionViewLayoutAttributes *attributes in array) {
        if (self.viewWidth > (position.x + attributes.frame.size.width)) {
            // cell fits on this row, place it
            position = [self placeCellAttributes:attributes atPosition:position];

        } else {
            // well the cell won't fit, need to find the best available position
            position = [self findBestPosition:position];

            // place the cell at the best remaining position
            position = [self placeCellAttributes:attributes atPosition:position];
        }
    }
}

/**
 Sets the cell attributes so the origin is on position.
 
 Returns the next open position on the same Y.
 */
- (CGPoint)placeCellAttributes:(UICollectionViewLayoutAttributes *)attributes atPosition:(CGPoint)position
{
    attributes.frame = CGRectMake(position.x, position.y, attributes.frame.size.width, attributes.frame.size.height);

    // store position information for when we have to wrap
    [self.candidatePositions addObject:[NSValue valueWithCGPoint:CGPointMake(position.x, position.y + attributes.frame.size.height)]];

    return CGPointMake(position.x + attributes.frame.size.width + self.spacerX, position.y);
}


/**
 Searches the list of available positions and returns one that should work
 */
- (CGPoint)findBestPosition:(CGPoint)currentPosition
{
    BOOL failedToFindAPosition = YES;

    // lets start with the worst possible position
    CGPoint bestPositionFound = CGPointMake(CGFLOAT_MAX, CGFLOAT_MAX);

    for (NSValue *value in self.candidatePositions) {
        CGPoint position = [value CGPointValue];

        // ignore positions higher than the current Y, we already know those are not good from previous passes.
        if (position.y > currentPosition.y) {

            // is this position lower than what we've got? Then it's the best!
            if (position.y < bestPositionFound.y) {
                bestPositionFound = position;
                failedToFindAPosition = NO;
            }
        }
    }

    // well we failed in our search, return the current position
    if (failedToFindAPosition) {
        return currentPosition;
    }

    // move the position down a bit and return
    return CGPointMake(bestPositionFound.x, bestPositionFound.y + self.spacerY);
}

@end
